package smp.edgecraft.uhc.core.teams;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class UHCPlayer
{
    private Player player;
    private UHCTeam team;
    private ChatColor teamColor;

    public UHCPlayer(Player player)
    {
        this.player = player;
    }

    public UHCPlayer(Player player, UHCTeam uhcTeam)
    {
        this.player = player;
        this.team = uhcTeam;
        setRespectiveColor();
    }

    public void setTeam(UHCTeam team)
    {
        this.team = team;
        setRespectiveColor();
    }

    public UHCTeam getTeam()
    {
        return team;
    }

    public Player getPlayer()
    {
        return player;
    }

    private void setRespectiveColor()
    {
        switch (team)
        {
            case BLUE:
                teamColor = ChatColor.BLUE;
                break;
            case RED:
                teamColor = ChatColor.RED;
                break;
            case YELLOW:
                teamColor = ChatColor.YELLOW;
                break;
            case GREEN:
                teamColor = ChatColor.GREEN;
                break;
            case PINK:
                teamColor = ChatColor.LIGHT_PURPLE;
                break;
            default:
                break;
        }

        player.setDisplayName(teamColor + player.getName() + ChatColor.RESET);
        player.setPlayerListName(teamColor + player.getName() + ChatColor.RESET);
    }

    public void setTeamColor(ChatColor teamColor)
    {
        this.teamColor = teamColor;
    }
}
