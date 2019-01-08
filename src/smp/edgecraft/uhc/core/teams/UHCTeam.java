package smp.edgecraft.uhc.core.teams;

import org.bukkit.ChatColor;
import smp.edgecraft.uhc.core.managers.UHCManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static smp.edgecraft.uhc.core.managers.UHCManager.CONFIG;

/**
 * Represents a team playing the UHC. Each team stores an instance of all the players in the team
 */
public class UHCTeam {

    public static List<UHCTeam> teams = new ArrayList<>();

    public static UHCTeam SPECTATOR;

    /**
     * The list of all the players on the team
     */
    private List<UHCPlayer> players;
    private String id;
    private ChatColor colour;
    private String name;
    private long vc;

    private UHCTeam(){
        this.id = "spectator";
        this.name = "&7&lSpectator";
        this.colour = ChatColor.GRAY;
        this.players = new ArrayList<>();
    }

    public UHCTeam(String id) {
        this.id = id;
        this.name = CONFIG.getString("teams " + id + " name");
        this.colour = ChatColor.getByChar(CONFIG.getString("teams " + id + " colour").replace("&", ""));
        this.vc = CONFIG.getLong("teams " + id + " vc");
        this.players = new ArrayList<>();
    }

    public long getVC() {
        return vc;
    }

    public static void createSpectatorTeam(){
        SPECTATOR = new UHCTeam();
    }

    public static UHCTeam get(String name){
        return teams.stream().filter(x -> x.id.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public static boolean exists(String name){
        return  get(name) != null || name.equals("null");
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
        teams.stream().filter(x -> x!=this).forEach(x -> x.removePlayer(player));
        CONFIG.set("players " + player.getPlayer().getUniqueId().toString() + " team", this.id); // Update the config
        player.getPlayer().setDisplayName(this.colour + player.getPlayer().getName() + ChatColor.RESET);
        player.getPlayer().setPlayerListName(this.colour + player.getPlayer().getName() + ChatColor.RESET);
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
        if (this.players.contains(player))
            this.players.remove(player);
        return this;
    }

    public String getDisplayName(){
        return ChatColor.translateAlternateColorCodes('&', name);
    }

    /**
     * @return whether the team is in use (i.e. there are players on the team)
     */
    public boolean hasPlayers() {
        return this.players.size() > 0;
    }

    public int size(){
        return this.players.size();
    }

    /**
     * @return the chat color of the team
     */
    public ChatColor getColour() {
        return this.colour;
    }
}
