package smp.edgecraft.uhc.core.commands;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.managers.UHCManager;

@CommandInfo(aliases = { "spectate" }, description = "Spectate the current uhc")
public class SpectateUHCCommand extends GameCommand {

    @Override
    public void onCommand(Player player, String[] args) {
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(UHCManager.WORLD_OVERWORLD.getSpawnLocation());
    }
}
