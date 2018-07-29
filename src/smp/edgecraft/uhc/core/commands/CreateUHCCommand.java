package smp.edgecraft.uhc.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.managers.UHCManager;

@CommandInfo(aliases = { "create" }, description = "Creates a new UHC")
public class CreateUHCCommand extends GameCommand {

    @Override
    public void onCommand(Player player, String[] args) {
        if (args.length == 0) {
            // Need world name
            player.sendMessage(ChatColor.RED + "Please specify a world name!");
        }
        else {
            UHCManager.createUHC(args[0]);
        }
    }
}
