package io.github.steaf23.bingoreloaded.gameloop.phase;

import io.github.steaf23.bingoreloaded.data.BingoMessage;
import io.github.steaf23.bingoreloaded.data.config.BingoConfigurationData;
import io.github.steaf23.bingoreloaded.data.config.BingoOptions;
import io.github.steaf23.bingoreloaded.event.BingoSettingsUpdatedEvent;
import io.github.steaf23.bingoreloaded.event.PlayerJoinedSessionWorldEvent;
import io.github.steaf23.bingoreloaded.event.PlayerLeftSessionWorldEvent;
import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import io.github.steaf23.bingoreloaded.player.BingoParticipant;
import io.github.steaf23.bingoreloaded.player.team.BingoTeam;
import io.github.steaf23.bingoreloaded.settings.PlayerKit;
import io.github.steaf23.bingoreloaded.util.timer.CountdownTimer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PostGamePhase implements GamePhase
{
    private final CountdownTimer timer;
    private final BingoSession session;
    private final BingoConfigurationData config;

    public PostGamePhase(BingoSession session, BingoConfigurationData config) {
        this.session = session;
        this.timer = new CountdownTimer(config.getOptionValue(BingoOptions.GAME_RESTART_TIME), session);
        this.config = config;
    }

    @Override
    public @Nullable BingoSession getSession() {
        return session;
    }

    @Override
    public void setup() {
        if (timer.getStartTime() <= 0) {
            session.prepareNextGame();
            timer.stop();
            return;
        }
        timer.start();
        timer.addNotifier(this::onTimerTicks);
        sendRestartMessage(timer.getTime(), session);
    }

    @Override
    public void end() {
        for (BingoTeam team : session.teamManager.getActiveTeams()) {
            team.setCard(null);
        }
        if (config.getOptionValue(BingoOptions.RESET_TEAMS_AFTER_GAME_ENDS)) {
            for (BingoParticipant participant : session.teamManager.getParticipants()) {
                session.teamManager.addMemberToTeam(participant, "auto");
            }
        }
    }

    @Override
    public void handlePlayerJoinedSessionWorld(PlayerJoinedSessionWorldEvent event) {
        sendRestartMessage(this.timer.getTime(), event.getPlayer());
    }

    @Override
    public void handlePlayerLeftSessionWorld(PlayerLeftSessionWorldEvent event) {

    }

    @Override
    public void handleSettingsUpdated(BingoSettingsUpdatedEvent event) {

    }

    @Override
    public void handlePlayerInteract(PlayerInteractEvent event) {
        BingoParticipant participant = session.teamManager.getPlayerAsParticipant(event.getPlayer());
        if (participant == null || participant.sessionPlayer().isEmpty())
            return;

        if (event.getItem() == null || event.getItem().getType().isAir())
            return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (PlayerKit.CARD_ITEM.isCompareKeyEqual(event.getItem())) {
            // Show bingo card to player
            event.setCancelled(true);
            participant.showCard(null);
        }
    }

    private void onTimerTicks(long timeLeft) {
        if (timeLeft == 0) {
            session.prepareNextGame();
            timer.stop();
        }
        else if (timeLeft == 5) {
            sendRestartMessage(timeLeft, session);
        }
    }

    public void sendRestartMessage(long timeLeft, Audience audience) {
        BingoMessage.POST_GAME_START.sendToAudience(audience, NamedTextColor.RED, Component.text(timeLeft).color(NamedTextColor.BLUE));
    }
}
