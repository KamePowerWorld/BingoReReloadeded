package io.github.steaf23.bingoreloaded.gui;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.settings.BingoSettingsBuilder;
import io.github.steaf23.easymenulib.menu.BasicMenu;
import io.github.steaf23.easymenulib.menu.MenuBoard;
import io.github.steaf23.easymenulib.menu.item.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;

import java.util.EnumSet;

public class EffectOptionsMenu extends BasicMenu
{
    private final EnumSet<EffectOptionFlags> flags;
    private final BingoSettingsBuilder settingsBuilder;

    public EffectOptionsMenu(MenuBoard menuBoard, BingoSettingsBuilder settings) {
        super(menuBoard, BingoTranslation.OPTIONS_EFFECTS.translate(), 6);
        this.settingsBuilder = settings;
        flags = settings.view().effects();

        for (int i = 0; i < 8; i++) {
            addItem(new MenuItem(i, 5, Material.BLACK_STAINED_GLASS_PANE, " "));
        }

        addEffectAction(EffectOptionFlags.NIGHT_VISION, 5, 3, Material.GOLDEN_CARROT);
        addEffectAction(EffectOptionFlags.WATER_BREATHING, 3, 3, Material.PUFFERFISH);
        addEffectAction(EffectOptionFlags.FIRE_RESISTANCE, 7, 3, Material.MAGMA_CREAM);
        addEffectAction(EffectOptionFlags.NO_FALL_DAMAGE, 2, 1, Material.NETHERITE_BOOTS);
        addEffectAction(EffectOptionFlags.SPEED, 1, 3, Material.FEATHER);
        addEffectAction(EffectOptionFlags.NO_DURABILITY, 6, 1, Material.NETHERITE_PICKAXE);
        addEffectAction(EffectOptionFlags.KEEP_INVENTORY, 4, 1, Material.CHEST);
        addCloseAction(new MenuItem(8, 5, Material.DIAMOND, "" + ChatColor.AQUA + ChatColor.BOLD + BingoTranslation.MENU_SAVE_EXIT.translate()));
    }

    private void addEffectAction(EffectOptionFlags flag, int slotX, int slotY, Material material) {
        MenuItem item = new MenuItem(slotX, slotY, material, "");
        updateUI(flag, item);
        addAction(item,
                player -> {
                    toggleOption(flag);
                    updateUI(flag, item);
                }
        );
    }

    private void toggleOption(EffectOptionFlags flag) {
        if (flags.contains(flag))
            flags.remove(flag);
        else
            flags.add(flag);
    }

    public void updateUI(EffectOptionFlags flag, MenuItem menuItem) {
        if (flags.contains(flag)) {
            menuItem.setName("" + ChatColor.GREEN + ChatColor.BOLD + flag.name + " " + BingoTranslation.EFFECTS_ENABLED.translate());
            menuItem.setLore(ChatColor.GREEN + BingoTranslation.EFFECTS_DISABLE.translate());
        } else {
            menuItem.setName("" + ChatColor.RED + ChatColor.BOLD + flag.name + " " + BingoTranslation.EFFECTS_DISABLED.translate());
            menuItem.setLore(ChatColor.RED + BingoTranslation.EFFECTS_ENABLE.translate());
        }
    }

    @Override
    public void beforeClosing(HumanEntity player) {
        settingsBuilder.effects(flags);
        super.beforeClosing(player);
    }
}
