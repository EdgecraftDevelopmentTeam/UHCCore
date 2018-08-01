package smp.edgecraft.uhc.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.managers.UHCManager;
import smp.edgecraft.uhc.core.teams.UHCPlayer;
import smp.edgecraft.uhc.core.teams.UHCTeam;

@CommandInfo(aliases = { "team" }, description = "Manually join a team")
public class TeamCommand extends GameCommand
{
    @Override
    public void onCommand(Player player, String[] args)
    {
        // uhc team <Team Name> <Player Name>
        if (args.length == 2)
        {
            String teamName = args[0];
            String givenPlayerName = args[1];
            UHCTeam givenTeam = null;
            ChatColor teamColor = null;

            teamName = teamName.toLowerCase();

            switch (teamName)
            {
                case "blue":
                    givenTeam = UHCTeam.BLUE;
                    teamColor = ChatColor.BLUE;
                    break;
                case "red":
                    givenTeam = UHCTeam.RED;
                    teamColor = ChatColor.RED;
                    break;
                case "yellow":
                    givenTeam = UHCTeam.YELLOW;
                    teamColor = ChatColor.YELLOW;
                    break;
                case "green":
                    givenTeam = UHCTeam.GREEN;
                    teamColor = ChatColor.GREEN;
                    break;
                case "pink":
                    givenTeam = UHCTeam.PINK;
                    teamColor = ChatColor.LIGHT_PURPLE;
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Invalid team name");
                    return;
            }

            String displayTeamName = teamName.substring(0,1).toUpperCase()
                    + teamName.substring(1, teamName.length());

            for (UHCPlayer uhcPlayer : UHCManager.PLAYERS)
            {
                if (uhcPlayer.getPlayer().getName().equalsIgnoreCase(givenPlayerName))
                {
                    uhcPlayer.setTeam(givenTeam);
                    uhcPlayer.getPlayer().sendMessage(ChatColor.YELLOW + "Your joined the "
                            + teamColor + displayTeamName + ChatColor.YELLOW + " team");

                    player.sendMessage(ChatColor.YELLOW + "Successfully put "
                            + uhcPlayer.getPlayer().getDisplayName() + ChatColor.YELLOW + "into the "
                            + teamColor + displayTeamName + ChatColor.YELLOW + " team");
                    return;
                }

            }
            // If the provided player name is not found:
            player.sendMessage(ChatColor.RED + "Invalid player name");
            return;
        }
    }
}
