package smp.edgecraft.uhc.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.teams.UHCPlayer;
import smp.edgecraft.uhc.core.teams.UHCTeam;

/**
 * Manually puts a player onto a specific team
 */
@CommandInfo(aliases = {"team"}, description = "Manually join a team", permission = "uhc.team")
public class TeamCommand extends GameCommand {
    @Override
    public void onCommand(Player player, String[] args) {
        // uhc team <Team Name> <Player Name>
        // or uhc team reset
        if (args.length == 2) {
            String teamName = args[0];
            String givenPlayerName = args[1];
            if (Bukkit.getPlayer(givenPlayerName) == null) {
                player.sendMessage(ChatColor.RED + "Invalid player name");
                return;
            }
            if (UHCTeam.exists(teamName.toLowerCase())) {
                player.sendMessage(ChatColor.RED + "Invalid team name");
                return;
            }
            UHCPlayer p = UHCPlayer.get(Bukkit.getPlayer(givenPlayerName));
            p.setTeam(UHCTeam.get(teamName.toLowerCase()));
            if (p.getTeam() == null) {
                p.getPlayer().sendMessage(ChatColor.YELLOW + "You left your team!");
                player.sendMessage(ChatColor.YELLOW + "Successfully made " + p.getPlayer().getDisplayName() + " leave their team!");
            } else {
                p.getPlayer().sendMessage(ChatColor.YELLOW + "You joined " + p.getTeam().getDisplayName());
                player.sendMessage(ChatColor.YELLOW + "Successfully put " + p.getPlayer().getDisplayName() + ChatColor.YELLOW + " into " + p.getTeam().getDisplayName());
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
            UHCPlayer.players.forEach(x -> x.setTeam(null));
            player.sendMessage(ChatColor.GREEN + "Reset teams!");
        }
    }
}
