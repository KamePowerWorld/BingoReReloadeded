package io.github.steaf23.easymenulib.inventory;

import io.github.steaf23.easymenulib.EasyMenuLibrary;
import io.github.steaf23.easymenulib.inventory.item.ItemTemplate;
import io.github.steaf23.easymenulib.util.ChatComponentUtils;
import io.github.steaf23.easymenulib.util.EasyMenuTranslationKey;
import net.md_5.bungee.api.ChatColor;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.function.Consumer;

public class UserInputMenu implements Menu
{
    record MenuTemplate(String title, Consumer<String> result, String startingText, HumanEntity player)
    {
    }

    private final MenuTemplate template;
    private AnvilGUI gui;
    private final MenuBoard board;
    private static final ItemTemplate EMPTY = new ItemTemplate(Material.ELYTRA, ChatComponentUtils.convert(EasyMenuTranslationKey.MENU_CLEAR_FILTER.translate(), ChatColor.GRAY, ChatColor.BOLD));
    private static final ItemTemplate ACCEPT = new ItemTemplate(Material.DIAMOND, ChatComponentUtils.convert(EasyMenuTranslationKey.MENU_ACCEPT.translate(),ChatColor.AQUA, ChatColor.BOLD));

    public UserInputMenu(MenuBoard manager, String title, Consumer<String> result, HumanEntity player, @NotNull String startingText) {
        this.board = manager;
        this.template = new MenuTemplate(title, result, startingText, player);
        this.board.open(this, player);
    }

    private AnvilGUI openAnvilUI(String title, Consumer<String> result, @NotNull String startingText, HumanEntity player) {
        return new AnvilGUI.Builder()
                .onClose(state -> board.close(this, state.getPlayer()))
                .title(BasicMenu.pluginTitlePrefix + title)
                .text(startingText.isEmpty() ? "name" : startingText)
                .itemRight(EMPTY.buildItem())
                .itemLeft(new ItemStack(Material.ELYTRA))
                .onClick((slot, state) -> {
                    if (slot == AnvilGUI.Slot.INPUT_RIGHT) {
                        // First close the anvil menu, then apply changes.
                        // Callers that want to react to the value changed when their menu gets reopened have to reopen their menu again...
                        // If we accept the result first we cant properly close out of it with linear code in some cases.
                        board.close(this, state.getPlayer());
                        result.accept("");
                        return Collections.emptyList();
                    } else if (slot == AnvilGUI.Slot.OUTPUT) {
                        board.close(this, state.getPlayer());
                        result.accept(state.getText());
                        return Collections.emptyList();
                    }
                    return Collections.emptyList();
                })
                .itemOutput(ACCEPT.buildItem())
                .plugin(EasyMenuLibrary.getPlugin())
                .open((Player) player);
    }

    @Override
    public void beforeOpening(HumanEntity player) {
        this.gui = openAnvilUI(template.title, template.result, template.startingText, template.player);
    }

    @Override
    public boolean onClick(InventoryClickEvent event, HumanEntity player, int clickedSlot, ClickType clickType) {
        return false;
    }

    @Override
    public boolean onDrag(InventoryDragEvent event) {
        return false;
    }

    @Override
    public void beforeClosing(HumanEntity player) {
    }

    @Override
    public Inventory getInventory() {
        return gui == null ? null : gui.getInventory();
    }

    @Override
    public void openInventory(HumanEntity player) {
    }

    @Override
    public void closeInventory(HumanEntity player) {
    }

    @Override
    public MenuBoard getMenuBoard() {
        return board;
    }

    @Override
    public boolean openOnce() {
        return true;
    }
}