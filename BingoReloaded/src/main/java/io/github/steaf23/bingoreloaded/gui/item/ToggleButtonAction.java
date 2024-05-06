package io.github.steaf23.bingoreloaded.gui.item;

import io.github.steaf23.bingoreloaded.gui.base.BasicMenu;
import io.github.steaf23.bingoreloaded.gui.base.Menu;
import io.github.steaf23.bingoreloaded.gui.base.item.MenuAction;
import io.github.steaf23.bingoreloaded.gui.base.item.MenuItem;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ToggleButtonAction extends MenuAction
{
    private boolean enabled;
    private final Function<Boolean, MenuItem> callback;

    public ToggleButtonAction(Function<Boolean, MenuItem> callback) {
        this(false, callback);
    }

    public ToggleButtonAction(boolean startEnabled, Function<Boolean, MenuItem> callback) {
        this.enabled = startEnabled;
        this.callback = callback;
    }

    @Override
    public void setItem(@NotNull MenuItem item) {
        super.setItem(item);
        item.setGlowing(enabled);

        item.addDescription("input", 10,
                Menu.inputButtonText("Left Click") + "increase",
                Menu.inputButtonText("Right Click") + "decrease",
                Menu.inputButtonText("Hold Shift") + "edit faster");
    }

    @Override
    public void use(BasicMenu.ActionArguments arguments) {
        enabled = !enabled;
        item = callback.apply(enabled);
        item.setGlowing(enabled);
    }
}
