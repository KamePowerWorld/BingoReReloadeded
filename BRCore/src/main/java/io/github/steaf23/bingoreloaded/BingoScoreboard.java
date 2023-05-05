package io.github.steaf23.bingoreloaded;

import io.github.steaf23.bingoreloaded.data.BingoTranslation;
import io.github.steaf23.bingoreloaded.event.BingoPlayerJoinEvent;
import io.github.steaf23.bingoreloaded.event.BingoPlayerLeaveEvent;
import io.github.steaf23.bingoreloaded.player.BingoPlayer;
import io.github.steaf23.bingoreloaded.player.BingoTeam;
import io.github.steaf23.bingoreloaded.player.TeamManager;
import io.github.steaf23.bingoreloaded.util.InfoScoreboard;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class BingoScoreboard
{
    private final Scoreboard teamBoard;
    private final InfoScoreboard visualBoard;
    private final Objective taskObjective;
    private final BingoSession session;
    private final boolean showPlayer;

    public BingoScoreboard(BingoSession session, boolean showPlayer)
    {
        this.session = session;
        this.showPlayer = showPlayer;
        this.teamBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.visualBoard = new InfoScoreboard("" + ChatColor.ITALIC + ChatColor.UNDERLINE + BingoTranslation.SCOREBOARD_TITLE.translate(), teamBoard);

        this.taskObjective = teamBoard.registerNewObjective("item_count", "bingo_item_count");

        reset();
    }

    public void updateTeamScores()
    {
        if (!session.isRunning())
            return;

        BingoReloadedCore.scheduleTask(task ->
        {
            Objective objective = teamBoard.getObjective("item_count");
            if (objective == null)
                return;

            for (BingoTeam t : session.teamManager.getActiveTeams())
            {
                if (t.card != null)
                {
                    objective.getScore("" + t.getColor().chatColor).setScore(t.card.getCompleteCount(t));
                }
            }
            updateVisual();
        });
    }

    public void updateVisual()
    {
        visualBoard.clearDisplay();

        TeamManager teamManager = session.teamManager;

        boolean condensedDisplay = !showPlayer
                || teamManager.getActiveTeams().size() + teamManager.getParticipants().size() > 13;

        visualBoard.setLineText(0, " ");
        int lineIndex = 1;
        for (BingoTeam team : teamManager.getActiveTeams())
        {
            String teamScoreLine = "" + ChatColor.DARK_RED + "[" + team.getColoredName().asLegacyString() + ChatColor.DARK_RED + "]" +
                    ChatColor.WHITE + ": " + ChatColor.BOLD + taskObjective.getScore("" + team.getColor().chatColor).getScore();
            visualBoard.setLineText(lineIndex, teamScoreLine);
            lineIndex += 1;

            if (!condensedDisplay)
            {
                for (BingoPlayer player : team.getPlayers())
                {
                    String playerLine = "" + ChatColor.GRAY + ChatColor.BOLD + " ┗ " + ChatColor.RESET + player.displayName;
                    visualBoard.setLineText(lineIndex, playerLine);
                    lineIndex += 1;
                }
            }
        }

        for (BingoPlayer p : teamManager.getParticipants())
        {
            updatePlayerScoreboard(p);
        }
    }

    public void reset()
    {
        BingoReloadedCore.scheduleTask(task -> {
            for (String entry : teamBoard.getEntries())
            {
                teamBoard.resetScores(entry);
            }

            for (BingoPlayer p : session.teamManager.getParticipants())
            {
                if (p.gamePlayer().isPresent())
                    visualBoard.clearPlayerBoard(p.gamePlayer().get());
            }

            updateTeamScores();
        });
    }

    public Scoreboard getTeamBoard()
    {
        return teamBoard;
    }

    public void handlePlayerJoin(final BingoPlayerJoinEvent event)
    {
        updatePlayerScoreboard(event.player);
    }

    public void handlePlayerLeave(final BingoPlayerLeaveEvent event)
    {
        updatePlayerScoreboard(event.player);
    }

    private void updatePlayerScoreboard(BingoPlayer player)
    {
        if (player.gamePlayer().isPresent())
        {
            if (player.session.isRunning())
                visualBoard.applyToPlayer(player.gamePlayer().get());
        }
        else if (player.asOnlinePlayer().isPresent() && player.asOnlinePlayer().get().getScoreboard().equals(teamBoard))
        {
            visualBoard.clearPlayerBoard(player.asOnlinePlayer().get());
        }
    }
}
