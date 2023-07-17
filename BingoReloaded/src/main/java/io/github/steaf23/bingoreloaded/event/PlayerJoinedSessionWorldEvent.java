package io.github.steaf23.bingoreloaded.event;

import io.github.steaf23.bingoreloaded.gameloop.BingoSession;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class PlayerJoinedSessionWorldEvent extends BingoEvent
{
    private final Player player;

    private final Location source;
    private final Location destination;
    private final boolean sourceIsBingoWorld;

    public PlayerJoinedSessionWorldEvent(Player player, BingoSession session, @Nullable Location source, @Nullable Location destination, @Nullable boolean sourceIsBingoWorld) {
        super(session);
        this.player = player;
        this.source = source;
        this.destination = destination;
        this.sourceIsBingoWorld = sourceIsBingoWorld;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getSource() {
        return source;
    }

    public boolean sourceIsBingoWorld() {
        return sourceIsBingoWorld;
    }

    public Location getDestination() {
        return destination;
    }
}
