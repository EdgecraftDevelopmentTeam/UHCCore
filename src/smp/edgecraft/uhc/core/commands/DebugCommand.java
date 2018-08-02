package smp.edgecraft.uhc.core.commands;

import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.managers.UHCManager;

@CommandInfo(aliases = { "debug" }, description = "Runs the debug command")
public class DebugCommand extends GameCommand {
    @Override
    public void onCommand(Player player, String[] args) {
        UHCManager.CONFIG.set("teams.prepared", false);
    }
}
