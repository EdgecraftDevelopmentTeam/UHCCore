package smp.edgecraft.uhc.core.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.managers.UHCManager;

@CommandInfo(aliases = { "prepare" }, description = "Prepares either the world or the teams")
public class PrepareCommand extends GameCommand {

    @Override
    public void onCommand(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Please choose whether to prepare the teams or the world");
            return;
        }

        if (args[0].equalsIgnoreCase("world")) UHCManager.prepareWorld();
        else if (args[0].equalsIgnoreCase("teams")) {
            UHCManager.prepareTeams();
        }
    }
}
