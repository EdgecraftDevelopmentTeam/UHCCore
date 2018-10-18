package smp.edgecraft.uhc.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.managers.UHCManager;

/**
 * The start command: starts the UHC match!
 */
@CommandInfo(aliases = {"start"}, description = "Starts the UHC", permission = "uhc.start")
public class StartCommand extends GameCommand {

    @Override
    public void onCommand(Player player, String[] args) {
        UHCManager.start();
    }

}
