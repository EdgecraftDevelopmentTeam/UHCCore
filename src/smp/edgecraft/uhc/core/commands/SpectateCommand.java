package smp.edgecraft.uhc.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.managers.UHCManager;
import smp.edgecraft.uhc.core.teams.UHCPlayer;
import smp.edgecraft.uhc.core.teams.UHCTeam;

/**
 * The spectate command: states the user wishes to spectate the UHC.
 */
@CommandInfo(aliases = {"spectate"}, description = "Spectate the current uhc", permission = "uhc.spectate")
public class SpectateCommand extends GameCommand {

    @Override
    public void onCommand(Player player, String[] args) {
        if (UHCManager.GAME_STATUS != UHCManager.GameStatus.LOBBY) // Ensure we are in the lobby
            return;
        for (UHCPlayer p: UHCManager.PLAYERS) {
            if (p.getPlayer().equals(player)) {
                if (p.getTeam() == UHCTeam.SPECTATOR) {
                    p.setTeam(UHCTeam.UNSET);
                    player.sendMessage(ChatColor.GREEN + "You are no longer spectating");
                } else {
                    p.setTeam(UHCTeam.SPECTATOR);
                    player.sendMessage(ChatColor.GREEN + "You are now spectating");
                }
                break;
            }
        }
    }
}
