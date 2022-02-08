package me.steven.bingoreloaded;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

public abstract class AbstractGUIInventory implements Listener
{
    protected Inventory inventory = null;
    public abstract void showInventory(HumanEntity player);
    //used to process logic when player clicks on an item in the inventory.
    public abstract void delegateClick(InventoryClickEvent event);
    //used to process logic when player drags an item in the inventory.
    public abstract void delegateDrag(InventoryDragEvent event);

    public AbstractGUIInventory()
    {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(BingoReloaded.NAME);
        if (plugin == null) return;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event)
    {
        if (inventory == null) return;
        if (event.getInventory() == inventory)
        {
            event.setCancelled(true);

            if (event.getRawSlot() < event.getInventory().getSize())
            {
                delegateClick(event);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent event)
    {
        if (inventory == null) return;
        if (event.getInventory() == inventory)
        {
            event.setCancelled(true);

            delegateDrag(event);
        }
    }
}
