package smp.edgecraft.uhc.core.commands;

import org.bukkit.entity.Player;

public abstract class GameCommand {
	
	public abstract void onCommand(Player player, String[] args);

}
