package smp.edgecraft.uhc.core.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandManager implements CommandExecutor {

	public static final CommandManager INSTANCE = new CommandManager();

	private ArrayList<GameCommand> commands;

	protected CommandManager() {
		this.commands = new ArrayList<>();

		this.commands.add(new JoinGameCommand());
		this.commands.add(new CreateGameCommand());
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can run mini game commands!");
			return true;
		}

		Player player = (Player) sender;
		if (command.getName().equalsIgnoreCase("hub")) {
			player.teleport(SettingsManager.getConfig("config").getLocation("hub"));
		}

		if (command.getName().equalsIgnoreCase("game")) {
			if (args.length == 0) {
				for (GameCommand cmd : this.commands) {
					CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
					player.sendMessage(ChatColor.GOLD + "/game (" + StringUtils.join(info.aliases(), " ").trim()
							+ ") - " + info.description());
				}

				return true;
			}

			GameCommand target = null;
			for (GameCommand cmd : this.commands) {
				CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
				for (String alias : info.aliases()) {
					if (args[0].equals(alias)) {
						target = cmd;
						break;
					}
				}
				if (target != null) {
					break;
				}
			}

			if (target == null) {
				player.sendMessage(ChatColor.RED + "Could not find command: /game " + args[0]);
				return true;
			}

			List<String> newArgs = new LinkedList<String>(Arrays.asList(args));
			newArgs.remove(0);
			target.onCommand(player, newArgs.toArray(new String[0]));
		}

		return true;
	}

}
