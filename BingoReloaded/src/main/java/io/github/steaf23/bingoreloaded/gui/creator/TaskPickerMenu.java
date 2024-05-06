package io.github.steaf23.bingoreloaded.gui.creator;

import io.github.steaf23.bingoreloaded.data.BingoCardData;
import io.github.steaf23.bingoreloaded.gui.base.FilterType;
import io.github.steaf23.bingoreloaded.gui.base.item.MenuItem;
import io.github.steaf23.bingoreloaded.gui.base.MenuBoard;
import io.github.steaf23.bingoreloaded.gui.base.PaginatedSelectionMenu;
import io.github.steaf23.bingoreloaded.item.ItemText;
import io.github.steaf23.bingoreloaded.tasks.BingoTask;
import io.github.steaf23.bingoreloaded.tasks.CountableTask;
import io.github.steaf23.bingoreloaded.tasks.TaskData;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskPickerMenu extends PaginatedSelectionMenu
{
    private final String listName;

    protected static final ItemText[] SELECTED_LORE = createSelectedLore();
    protected static final ItemText[] UNSELECTED_LORE = createUnselectedLore();

    public TaskPickerMenu(MenuBoard manager, String title, List<BingoTask> options, String listName) {
        super(manager, title, asPickerItems(options), FilterType.DISPLAY_NAME);
        this.listName = listName;
        this.setMaxStackSizeOverride(64);
    }

    @Override
    public void onOptionClickedDelegate(InventoryClickEvent event, MenuItem clickedOption, HumanEntity player) {
        switch (event.getClick()) {
            case LEFT -> incrementItemCount(clickedOption, 1);
            case SHIFT_LEFT -> incrementItemCount(clickedOption, 10);
            case RIGHT -> decrementItemCount(clickedOption, 1);
            case SHIFT_RIGHT -> decrementItemCount(clickedOption, 10);
        }
    }

    public TaskData incrementItemCount(MenuItem item, int by) {
        // When entering this method, the item always needs to be selected by the end.
        // Now just check if the item was already selected prior to this moment.
        boolean alreadySelected = getSelectedItems().contains(item);

        int newAmount = item.getAmount();
        if (alreadySelected) {
            newAmount = Math.min(64, newAmount + by);
            if (newAmount == item.getAmount())
                return null;
        }

        TaskData newData = BingoTask.fromMenuItem(item).data;
        MenuItem newItem = getUpdatedTaskItem(newData, true, newAmount)
                .copyToSlot(item.getSlot());
        replaceItem(newItem, newItem.getSlot());
        selectItem(newItem, true);

        return newData;
    }

    public TaskData decrementItemCount(MenuItem item, int by) {
        // When entering this method the item could already be deselected, in which case we return;
        boolean deselect = false;
        if (!getSelectedItems().contains(item)) {
            return null;
        }

        // If the item is selected and its amount is set to 1 prior to this, then deselect it
        if (item.getAmount() == 1) {
            deselect = true;
        }

        int newAmount = item.getAmount();
        if (!deselect) {
            newAmount = Math.max(1, newAmount - by);
        }

        TaskData newData = BingoTask.fromMenuItem(item).data;
        MenuItem newItem = getUpdatedTaskItem(newData, !deselect, newAmount)
                .copyToSlot(item.getSlot());
        replaceItem(newItem, newItem.getSlot());
        selectItem(newItem, !deselect);

        return newData;
    }

    @Override
    public void beforeOpening(HumanEntity player) {
        super.beforeOpening(player);

        BingoCardData cardsData = new BingoCardData();
        Set<TaskData> tasks = cardsData.lists().getTasks(listName, true, true);

        for (MenuItem item : getAllItems()) {
            TaskData itemData = BingoTask.fromMenuItem(item).data;
            TaskData savedTask = null;
            for (var t : tasks) {
                if (t.isTaskEqual(itemData)) {
                    savedTask = t;
                    break;
                }
            }

            if (savedTask != null) {
                int count = 1;
                if (savedTask instanceof CountableTask countable)
                    count = countable.getCount();

                MenuItem newItem = getUpdatedTaskItem(itemData, true, count);
                replaceItem(newItem, item);
                selectItem(newItem, true);
            }
        }
    }

    @Override
    public void beforeClosing(HumanEntity player) {
        super.beforeClosing(player);

        BingoCardData cardsData = new BingoCardData();
        cardsData.lists().saveTasksFromGroup(listName,
                getAllItems().stream().map(item -> BingoTask.fromMenuItem(item).data).collect(Collectors.toList()),
                getSelectedItems().stream().map(item -> BingoTask.fromMenuItem(item).data).collect(Collectors.toList()));
    }

    public static List<MenuItem> asPickerItems(List<BingoTask> tasks) {
        List<MenuItem> result = new ArrayList<>();
        tasks.forEach(task -> {
            MenuItem item = getUpdatedTaskItem(task.data, false, 1);
            item.setGlowing(false);
            result.add(item);
        });
        return result;
    }

    private static MenuItem getUpdatedTaskItem(TaskData old, boolean selected, int newCount) {
        TaskData newData = old;
        if (selected) {
            if (newData instanceof CountableTask countable) {
                newData = countable.updateTask(newCount);
            }
        }

        BingoTask newTask = new BingoTask(newData);
        MenuItem item = newTask.asMenuItem();

        String[] addedLore;
        if (selected)
            addedLore = Arrays.stream(SELECTED_LORE)
                    .map(ItemText::asLegacyString)
                    .toList().toArray(new String[]{});
        else
            addedLore = Arrays.stream(UNSELECTED_LORE)
                    .map(ItemText::asLegacyString)
                    .toList().toArray(new String[]{});

        String[] taskDescription = Arrays.stream(newTask.data.getItemDescription())
                .map(ItemText::asLegacyString)
                .toList().toArray(new String[]{});

        item.setLore(taskDescription);
        item.addDescription("selected", 5, addedLore);
        return item;
    }

    private static ItemText[] createSelectedLore() {
        var text = new ItemText(" - ", ChatColor.WHITE, ChatColor.ITALIC);
        text.addText("This task has been added to the list", ChatColor.DARK_PURPLE);
        return new ItemText[]{text};
    }

    private static ItemText[] createUnselectedLore() {
        var text = new ItemText(" - ", ChatColor.WHITE, ChatColor.ITALIC);
        text.addText("Click to make this task", ChatColor.GRAY);
        var text2 = new ItemText("   appear on bingo cards", ChatColor.GRAY, ChatColor.ITALIC);
        return new ItemText[]{text, text2};
    }
}
