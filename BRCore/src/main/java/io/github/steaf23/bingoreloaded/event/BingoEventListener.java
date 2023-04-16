package io.github.steaf23.bingoreloaded.event;

import io.github.steaf23.bingoreloaded.BingoGame;
import io.github.steaf23.bingoreloaded.BingoSession;
import io.github.steaf23.bingoreloaded.tasks.statistics.StatisticTracker;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.function.Function;

public class BingoEventListener implements Listener
{
    private final Function<World, BingoSession> sessionResolver;
    private final boolean useAdvancements;
    private final boolean useStatistics;

    public BingoEventListener(Function<World, BingoSession> sessionResolver, boolean useAdvancements, boolean useStatistics)
    {
        this.sessionResolver = sessionResolver;
        this.useAdvancements = useAdvancements;
        this.useStatistics = useStatistics;
    }

    private BingoSession getSession(World world)
    {
        return sessionResolver.apply(world);
    }

    @EventHandler
    public void handleBingoGameEnded(final BingoEndedEvent event)
    {
        if (event.session != null)
        {
            event.session.handleGameEnded(event);
        }
    }

    @EventHandler
    public void handleBingoTaskComplete(final BingoCardTaskCompleteEvent event)
    {
        BingoGame game = event.session != null ? event.session.game() : null;
        if (game != null)
        {
            game.handleBingoTaskComplete(event);
            game.getCardEventManager().handleTaskCompleted(event);
        }
    }

    @EventHandler
    public void handlePlayerDropItem(final PlayerDropItemEvent event)
    {
        BingoSession session = getSession(event.getPlayer().getWorld());
        BingoGame game = session != null ? session.game() : null;
        if (game != null)
        {
            game.handlePlayerDropItem(event);
            game.getCardEventManager().handlePlayerDroppedItem(event, game);
        }
    }

    @EventHandler
    public void handlePlayerInteract(final PlayerInteractEvent event)
    {
        BingoSession session = getSession(event.getPlayer().getWorld());
        BingoGame game = session != null ? session.game() : null;
        if (game != null)
        {
            game.handlePlayerInteract(event);
        }
    }

    @EventHandler
    public void handleEntityDamage(final EntityDamageEvent event)
    {
        BingoSession session = getSession(event.getEntity().getWorld());
        BingoGame game = session != null ? session.game() : null;
        if (game != null)
        {
            game.handleEntityDamage(event);
        }
    }

    @EventHandler
    public void handlePlayerDeath(final PlayerDeathEvent event)
    {
        BingoSession session = getSession(event.getEntity().getWorld());
        BingoGame game = session != null ? session.game() : null;
        if (game != null)
        {
            game.handlePlayerDeath(event);
        }
    }

    @EventHandler
    public void handlePlayerRespawn(final PlayerRespawnEvent event)
    {
        BingoSession session = getSession(event.getPlayer().getWorld());
        BingoGame game = session != null ? session.game() : null;
        if (game != null)
        {
            game.handlePlayerRespawn(event);
        }
    }

    @EventHandler
    public void handleCountdownFinished(final CountdownTimerFinishedEvent event)
    {
        BingoGame game = event.session != null ? event.session.game() : null;
        if (game != null)
        {
            game.handleCountdownFinished(event);
        }
    }

    @EventHandler
    public void handlePlayerJoin(final BingoPlayerJoinEvent event)
    {
        BingoSession session = event.session;
        if (session != null)
        {
            session.scoreboard.handlePlayerJoin(event);
        }
    }

    @EventHandler
    public void handlePlayerLeave(final BingoPlayerLeaveEvent event)
    {
        BingoSession session = event.session;
        if (session != null)
        {
            session.scoreboard.handlePlayerLeave(event);
        }
    }

    @EventHandler
    public void handlePlayerAdvancementCompleted(final PlayerAdvancementDoneEvent event)
    {
        if (!useAdvancements)
            return;

        BingoSession session = getSession(event.getPlayer().getWorld());
        BingoGame game = session != null ? session.game() : null;
        if (game != null)
        {
            game.getCardEventManager().handlePlayerAdvancementCompleted(event, getSession(event.getPlayer().getWorld()));
        }
    }

    @EventHandler
    public void handlePlayerPickupItem(final EntityPickupItemEvent event)
    {
        BingoSession session = getSession(event.getEntity().getWorld());
        BingoGame game = session != null ? session.game() : null;
        if (game != null)
        {
            game.getCardEventManager().handlePlayerPickupItem(event, game);
        }
    }

    @EventHandler
    public void handleInventoryClicked(final InventoryClickEvent event)
    {
        BingoSession session = getSession(event.getWhoClicked().getWorld());
        BingoGame game = session != null ? session.game() : null;
        if (game != null)
        {
            game.getCardEventManager().handleInventoryClicked(event, game);
        }
    }

    @EventHandler
    public void handlePlayerJoinsServer(final PlayerJoinEvent event)
    {
        BingoSession session = getSession(event.getPlayer().getWorld());
        if (session != null)
        {
            session.teamManager.handlePlayerJoinsServer(event);
        }
    }

    @EventHandler
    public void handlePlayerChangedWorld(final PlayerChangedWorldEvent event)
    {
        BingoSession session = getSession(event.getPlayer().getWorld());
        if (session == null)
        {
            session = getSession(event.getFrom());
            if (session == null)
                return;
        }
        session.teamManager.handlePlayerChangedWorld(event);
    }

    @EventHandler
    public void handleStatisticIncrement(final PlayerStatisticIncrementEvent event)
    {
        if (!useStatistics)
            return;

        BingoSession session = getSession(event.getPlayer().getWorld());
        BingoGame game = session != null ? session.game() : null;
        if (game != null)
        {
            StatisticTracker tracker = game.getStatisticTracker();
            if (tracker != null)
                tracker.handleStatisticIncrement(event, game);
        }
    }

    @EventHandler
    public void handleStatisticCompleted(final BingoStatisticCompletedEvent event)
    {
        if (!useStatistics)
            return;

        BingoGame game = event.session != null ? event.session.game() : null;
        if (game != null)
        {
            game.getCardEventManager().handleStatisticCompleted(event, game);
        }
    }
}
