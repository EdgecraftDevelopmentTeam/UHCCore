package smp.edgecraft.uhc.core.teams;

import org.bukkit.ChatColor;
import smp.edgecraft.uhc.core.managers.SettingsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a team playing the UHC. Each team stores an instance of all the players in the team
 */
public enum UHCTeam {
    UNSET(ChatColor.GRAY), // The default team of every player
    BLUE(ChatColor.BLUE),
    RED(ChatColor.RED),
    YELLOW(ChatColor.YELLOW),
    GREEN(ChatColor.GREEN),
    PINK(ChatColor.LIGHT_PURPLE);

    /**
     * The list of all the players on the team
     */
    private List<UHCPlayer> players;
    private ChatColor teamColor;

    /**
     * Represents a team playing the UHC.
     */
    UHCTeam(ChatColor teamColor) {
        this.teamColor = teamColor;
        this.players = new ArrayList<>();
    }

    /**
     * @return the list of players on the team
     */
    public List<UHCPlayer> getPlayers() {
        return this.players;
    }

    /**
     * Override the players on this team
     *
     * @param players The players to put on this team
     */
    public void setPlayers(List<UHCPlayer> players) {
        this.players = players;
    }

    /**
     * Adds the given player to this team
     *
     * @param player The player to add
     * @return The updated team
     */
    public UHCTeam addPlayer(UHCPlayer player) {
        if (UNSET.getPlayers().contains(player))
            UNSET.removePlayer(player);
        player.getPlayer().setDisplayName(this.teamColor + player.getPlayer().getName() + ChatColor.RESET);
        player.getPlayer().setPlayerListName(this.teamColor + player.getPlayer().getName() + ChatColor.RESET);
        this.players.add(player);
        return this;
    }

    /**
     * Removes a player from this team
     *
     * @param player The player to remove
     * @return The updated team
     */
    public UHCTeam removePlayer(UHCPlayer player) {
        if (this.players.contains(player)) {
            if (this != UNSET)
                UNSET.addPlayer(player);
            this.players.remove(player);
        }
        return this;
    }

    /**
     * @return whether the team is in use (i.e. there are players on the team)
     */
    public boolean isActive() {
        return this.players.size() > 0;
    }
}
