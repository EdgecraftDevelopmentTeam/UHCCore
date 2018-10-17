package smp.edgecraft.uhc.core.commands;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.managers.UHCManager;

/**
 * The spectate command: states the user wishes to spectate the UHC. TODO FINISH
 */
@CommandInfo(aliases = {"spectate"}, description = "Spectate the current uhc")
public class SpectateCommand extends GameCommand {

    @Override
    public void onCommand(Player player, String[] args) {
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(UHCManager.WORLD_OVERWORLD.getSpawnLocation());
    }
}
