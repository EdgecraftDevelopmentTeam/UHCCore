package smp.edgecraft.uhc.core.teams;

import net.dv8tion.jda.core.entities.Member;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static smp.edgecraft.uhc.core.teams.UHCTeam.SPECTATOR;

/**
 * Represents a player who is playing in the UHC
 */
public class UHCPlayer {

    public static List<UHCPlayer> players = new ArrayList<>();

    /**
     * The instance of the player.
     *
     * @see Player
     */
    private Player player;
    /**
     * The team the player is on
     */
    private UHCTeam team;
    /**
     * The discord account connected to the player
     */
    private Member discordMember;

    /**
     * Create a new UHC player
     *
     * @param player the player object
     */
    public UHCPlayer(Player player) {
        this.player = player;
        players.add(this);
    }

    /**
     * Link a discord member to this player
     *
     * @param member The member to link
     * @return The update player
     */
    public UHCPlayer link(Member member) {
        this.discordMember = member;
        return this;
    }

    /**
     * @return the discord member linked to this player
     */
    @Nullable
    public Member getDiscordMember() {
        return this.discordMember;
    }

    /**
     * @return which team the player is on
     */
    public UHCTeam getTeam() {
        return this.team;
    }

    /**
     * Sets which team this player is on, removing the player from the previous team, and adding the player to the new team.
     * Also, this will update the color of the name of the player
     *
     * @param team The team to switch the player onto
     * @return The updated player
     */
    public UHCPlayer setTeam(UHCTeam team) {
        if(this.team != null) this.team.removePlayer(this);
        if(team != null) team.addPlayer(this);
        this.team = team;
        return this;
    }

    public boolean hasTeam(){
        return team != null;
    }

    public static UHCPlayer get(Player p){
        return players.stream().filter(x -> x.getPlayer() == p).findFirst().orElse(null);
    }

    public static boolean hasUHCPlayer(Player p){
        return get(p) != null;
    }

    public void assertTeam(){
        List<UHCTeam> l = UHCTeam.teams.stream().filter(x -> x!=SPECTATOR).filter(x -> x.size() < Math.ceil(players.size() / UHCTeam.teams.size())).collect(Collectors.toList());
        Collections.shuffle(l);
        setTeam(l.get(0));
    }

    /**
     * @return return the instance of the player object
     */
    public Player getPlayer() {
        return this.player;
    }
}
