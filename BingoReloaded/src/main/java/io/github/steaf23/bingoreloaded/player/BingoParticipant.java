package io.github.steaf23.bingoreloaded.player;

import io.github.steaf23.bingoreloaded.cards.TaskCard;
import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.gui.inventory.EffectOptionFlags;
import io.github.steaf23.bingoreloaded.player.team.BingoTeam;
import io.github.steaf23.bingoreloaded.settings.PlayerKit;
import io.github.steaf23.bingoreloaded.tasks.GameTask;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.map.MapRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public interface BingoParticipant extends ForwardingAudience.Single
{
    BingoSession getSession();
    @Nullable
    BingoTeam getTeam();
    void setTeam(@Nullable BingoTeam team);
    UUID getId();
    Optional<Player> sessionPlayer();
    String getName();
    Component getDisplayName();
    void showDeathMatchTask(GameTask task);
    void showCard(GameTask deathMatchTask);
    boolean alwaysActive();
    default int getAmountOfTaskCompleted() {
        if (getTeam() == null) {
            return 0;
        }

        Optional<TaskCard> card = getTeam().getCard();
        return card.map(taskCard -> taskCard.getCompleteCount(this)).orElse(0);
    }

    default Optional<TaskCard> getCard() {
        if (getTeam() == null) {
            return Optional.empty();
        }
        return getTeam().getCard();
    }

    void giveBingoCard(int cardSlot, @Nullable MapRenderer renderer);
    void giveEffects(EnumSet<EffectOptionFlags> effects, int gracePeriod);
    void takeEffects(boolean force);
    void giveKit(PlayerKit kit);
}
