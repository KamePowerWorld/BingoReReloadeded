package io.github.steaf23.bingoreloaded.gui.item;

import io.github.steaf23.bingoreloaded.data.TeamData;
import io.github.steaf23.bingoreloaded.gui.base.BasicMenu;
import io.github.steaf23.bingoreloaded.gui.base.MenuBoard;
import io.github.steaf23.bingoreloaded.gui.base.UserInputMenu;
import io.github.steaf23.bingoreloaded.gui.base.item.MenuAction;
import org.bukkit.entity.HumanEntity;

import java.util.function.Consumer;

public class NameEditAction extends MenuAction
{
    private String value;
    private final Consumer<String> callback;
    private final MenuBoard board;
    private final String prompt;

    public NameEditAction(String prompt, MenuBoard board, Consumer<String> callback) {
        this.callback = callback;
        this.board = board;
        this.prompt = prompt;
    }

    @Override
    public void use(BasicMenu.ActionArguments arguments) {
        renameItem("", arguments.player());
    }

    protected void renameItem(String format, HumanEntity player) {
        new UserInputMenu(board, prompt, (result) -> {
            value = result;
            item.setName(format + value);
            callback.accept(value);
        }, player, value);
    }
}
