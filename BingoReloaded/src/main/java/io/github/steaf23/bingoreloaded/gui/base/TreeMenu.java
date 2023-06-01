package io.github.steaf23.bingoreloaded.gui.base;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;

public class TreeMenu extends ActionMenu
{
    private static final MenuItem BACK = new MenuItem(0, Material.BARRIER, BingoTranslation.MENU_PREV.translate());
    private final Map<MenuItem, MenuInventory> options;

    public TreeMenu(int size, String title, MenuInventory parent)
    {
        super(size, title, parent);
        this.options = new HashMap<>();
    }

    public TreeMenu addMenuAction(MenuItem button, MenuInventory option)
    {
        options.put(button, option);
        addItem(button);
        return this;
    }

    @Override
    public void onItemClicked(InventoryClickEvent event, int slotClicked, Player player, ClickType clickType)
    {
        if (slotClicked == BACK.getSlot())
        {
            close(player);
            return;
        }

        for (var option : options.entrySet())
        {
            if (option.getKey().getSlot() == slotClicked)
            {
                option.getValue().open(player);
                return;
            }
        }
    }
}