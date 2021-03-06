package smp.edgecraft.uhc.core.commands;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import smp.edgecraft.uhc.core.managers.UHCManager;

/**
 * The prepare command which just prepares the teams. Must be ran before starting the game
 */
@CommandInfo(aliases = {"prepare"}, description = "Prepares the teams", permission = "uhc.prepare")
public class PrepareCommand extends GameCommand {

    @Override
    public void onCommand(Player player, String[] args) {
        UHCManager.prepareTeams();
    }
}
