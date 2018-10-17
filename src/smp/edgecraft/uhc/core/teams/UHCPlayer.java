package smp.edgecraft.uhc.core.teams;

import net.dv8tion.jda.core.entities.Member;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;

/**
 * Represents a player who is playing in the UHC
 */
public class UHCPlayer {
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
        this.team = UHCTeam.UNSET.addPlayer(this);
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
        this.team.removePlayer(this);
        team.addPlayer(this);
        this.team = team;
        return this;
    }

    /**
     * @return return the instance of the player object
     */
    public Player getPlayer() {
        return this.player;
    }
}
