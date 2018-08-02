package smp.edgecraft.uhc.core.commands;

import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.managers.UHCManager;

@CommandInfo(aliases = { "prepare" }, description = "Prepares either the world or the teams")
public class PrepareCommand extends GameCommand {

    @Override
    public void onCommand(Player player, String[] args) {
        UHCManager.prepareTeams();
    }
}
