package smp.edgecraft.uhc.core.managers;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.commands.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles all of the commands the UHC uses
 */
public class CommandManager implements CommandExecutor {

    /**
     * The instance of the command manager
     */
    public static final CommandManager INSTANCE = new CommandManager();

    /**
     * The registered commands
     */
    private ArrayList<GameCommand> commands;

    CommandManager() {
        this.commands = new ArrayList<>();

        this.commands.add(new SpectateCommand());
        this.commands.add(new PrepareCommand());
        this.commands.add(new StartCommand());
        this.commands.add(new TeamCommand());
        this.commands.add(new UpdateCommand());
        this.commands.add(new LinkCommand());
        this.commands.add(new DebugCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can run uhc commands!");
            return true;
        }

        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("uhc")) { // Only use commands which begin with "uhc"
            if (args.length == 0) {
                // Prints the help commands
                for (GameCommand cmd : this.commands) {
                    CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
                    player.sendMessage(ChatColor.GOLD + "/uhc " + StringUtils.join(info.aliases(), ", ").trim()
                            + " - " + info.description());
                }

                return true;
            }

            // Find the correct command
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

            if (target == null) { // If no command was found
                player.sendMessage(ChatColor.RED + "Could not find command: /uhc " + args[0]);
                return true;
            }

            // Otherwise execute the correct command
            List<String> newArgs = new LinkedList<>(Arrays.asList(args));
            newArgs.remove(0);
            target.onCommand(player, newArgs.toArray(new String[0]));
        }

        return true;
    }

}
