package io.github.steaf23.bingoreloaded.gui.item;

import io.github.steaf23.bingoreloaded.gui.base.BasicMenu;
import io.github.steaf23.bingoreloaded.gui.base.Menu;
import io.github.steaf23.bingoreloaded.gui.base.item.MenuAction;
import io.github.steaf23.bingoreloaded.gui.base.item.MenuItem;
import io.github.steaf23.bingoreloaded.util.ExtraMath;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class SpinBoxButtonAction extends MenuAction {
    private int value;
    private final int min;
    private final int max;
    private final BiConsumer<Integer, MenuItem> callback;

    public SpinBoxButtonAction(int minValue, int maxValue, int initialValue, BiConsumer<Integer, MenuItem> callback) {
        this.min = ExtraMath.clamped(minValue, 1, maxValue);
        this.max = ExtraMath.clamped(maxValue, minValue, 64);
        this.value = ExtraMath.clamped(initialValue, min, max);
        this.callback = callback;
    }

    @Override
    public void setItem(@NotNull MenuItem item) {
        super.setItem(item);
        item.setAmount(value);

        item.setDescription();
    }

    @Override
    public void use(BasicMenu.ActionArguments arguments) {
        int changeBy = 1;
        if (arguments.clickType().isShiftClick()) {
            changeBy = 10;
        }

        if (arguments.clickType().isRightClick()) {
            changeBy *= -1;
        }

        value = ExtraMath.clamped(value + changeBy, min, max);
        if (item != null) {
            item.setAmount(value);
        }
        callback.accept(value, item);
    }

    public void updateDescription(String... newDescription) {
        List<String> newDesc = Arrays.stream(newDescription).toList();
        newDesc.addAll(List.of(
                Menu.inputButtonText("Left Click") + "increase",
                Menu.inputButtonText("Right Click") + "decrease",
                Menu.inputButtonText("Hold Shift") + "edit faster"
        ));
        item.setDescription(newDesc.toArray(new String[]{}));
    }
}
