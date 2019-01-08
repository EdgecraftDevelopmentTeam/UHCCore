package smp.edgecraft.uhc.core.discord;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.managers.UHCManager;
import smp.edgecraft.uhc.core.teams.UHCPlayer;

import java.util.HashMap;
import java.util.Random;

/**
 * The discord bot which handles all things to do with the UHC and discord
 */
public class UHCBot extends ListenerAdapter {

    /**
     * The instance of the discord-java library
     */
    public static JDA jda;
    /**
     * The instance of the server we are connected to
     */
    public static Guild guild;
    /**
     * The controller of the guild
     *
     * @see Guild#getController()
     */
    public static GuildController guildController;

    /**
     * The instance of the main voice channel
     */
    public static VoiceChannel mainChannel;
    /**
     * The instance of the blue voice channel
     */
    public static VoiceChannel blueChannel;
    /**
     * The instance of the red voice channel
     */
    public static VoiceChannel redChannel;
    /**
     * The instance of the yellow voice channel
     */
    public static VoiceChannel yellowChannel;
    /**
     * The instance of the green voice channel
     */
    public static VoiceChannel greenChannel;
    /**
     * The instance of the pink voice channel
     */
    public static VoiceChannel pinkChannel;

    /**
     * All of the randomly generated link codes and the players they were sent to
     */
    private static HashMap<String, Player> linkData = new HashMap<>();

    /**
     * Initialise all of the instances
     */
    public static void onEnable() {
        try {
            // Create the discord bot
            jda = new JDABuilder(AccountType.BOT).setToken(UHCManager.CONFIG.get("discord.token")).addEventListener(new UHCBot()).buildBlocking();
            // Get the server
            guild = jda.getGuildById(String.valueOf(UHCManager.CONFIG.<Long>get("discord.guild")));
            guildController = guild.getController();
            // Set the "playing" text
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Game.of(Game.GameType.DEFAULT, "UUHC"));
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.setAutoReconnect(true);
            // Initialise voice channel references
            mainChannel = guild.getVoiceChannelById(UHCManager.CONFIG.<Long>get("discord.vcs.main"));
            blueChannel = guild.getVoiceChannelById(UHCManager.CONFIG.<Long>get("discord.vcs.blue"));
            redChannel = guild.getVoiceChannelById(UHCManager.CONFIG.<Long>get("discord.vcs.red"));
            yellowChannel = guild.getVoiceChannelById(UHCManager.CONFIG.<Long>get("discord.vcs.yellow"));
            greenChannel = guild.getVoiceChannelById(UHCManager.CONFIG.<Long>get("discord.vcs.green"));
            pinkChannel = guild.getVoiceChannelById(UHCManager.CONFIG.<Long>get("discord.vcs.pink"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shutdown the bot
     */
    public static void onDisable() {
        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
        jda.shutdownNow();
    }

    /**
     * Move all of the online players who have discord linked to the appropriate channel for their team
     */
    public static void movePlayersInVC() {
        for (UHCPlayer player : UHCManager.PLAYERS) {
            // Is the player linked with discord and are they on the main voice channel?
            if (player.getDiscordMember() != null && mainChannel.getMembers().contains(player.getDiscordMember())) {
                switch (player.getTeam()) {
                    case BLUE:
                        guildController.moveVoiceMember(player.getDiscordMember(), blueChannel).queue();
                        break;
                    case RED:
                        guildController.moveVoiceMember(player.getDiscordMember(), redChannel).queue();
                        break;
                    case YELLOW:
                        guildController.moveVoiceMember(player.getDiscordMember(), yellowChannel).queue();
                        break;
                    case GREEN:
                        guildController.moveVoiceMember(player.getDiscordMember(), greenChannel).queue();
                        break;
                    case PINK:
                        guildController.moveVoiceMember(player.getDiscordMember(), pinkChannel).queue();
                        break;
                }
            }
        }
    }

    /**
     * Move the given player to the main voice channel (e.g. when they die)
     *
     * @param player The player to move
     */
    public static void movePlayerToMainVC(UHCPlayer player) {
        if (mainChannel.getMembers().contains(player.getDiscordMember()))
            guildController.moveVoiceMember(player.getDiscordMember(), mainChannel).queue();
    }

    /**
     * Try to link the given player to their own discord account.
     * This will first check if they already have got a linked discord account,
     * if not we will then check if they already have had a link given to them and if not,
     * we will generate a new string of text the user must use to link their discord account
     *
     * @param player The player to link
     */
    public static void link(Player player) {
        for (UHCPlayer player1 : UHCManager.PLAYERS) {
            if (player1.getPlayer().equals(player) && player1.getDiscordMember() != null) {
                player.sendMessage(ChatColor.RED + "A discord account (" + player1.getDiscordMember().getUser().getName() + ") is already linked with this Minecraft account");
                return;
            }
        }
        if (linkData.containsValue(player)) {
            player.sendMessage(ChatColor.RED + "You have already generated a link command!");
            return;
        }
        String randomData = String.valueOf(new Random().nextInt(9999));
        while (linkData.containsKey(randomData))
            randomData = String.valueOf(new Random().nextInt(9999));
        linkData.put(randomData, player);
        player.sendMessage(ChatColor.GOLD + "Please message the UHCCore Discord Bot this message: " + ChatColor.WHITE + "!link " + randomData);
    }

    /**
     * Called when a message is sent either in the discord server or in the bot dm.
     *
     * @param event The event to hook into
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentDisplay().startsWith("!link ")) { // Checks if the user typed !link in discord
            if (linkData.containsKey(event.getMessage().getContentDisplay().substring(6))) { // Checks if there is a player linked to the data provided
                Player player = linkData.get(event.getMessage().getContentDisplay().substring(6)); // Get the player linked to the data provided
                for (UHCPlayer player1 : UHCManager.PLAYERS) {
                    if (player1.getPlayer().equals(player)) {
                        UHCManager.CONFIG.set("players." + player.getUniqueId().toString() + ".discord", event.getAuthor().getId()); // Update the config
                        player1.link(event.getMember()); // Link the account
                        player.sendMessage(ChatColor.GREEN + "Sucessfully linked discord user (" + event.getAuthor().getName() + ") with your Minecraft account!"); // Notify the player
                        event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(event.getAuthor().getAsMention() + " you have sucessfully linked your Minecraft account (" + player.getName() + ") with your discord account!").queue()); // Privately message the user on discord
                        linkData.remove(event.getMessage().getContentDisplay().substring(6)); // Remove the link data
                        event.getMessage().delete();
                        break;
                    }
                }
            }
        }
    }
}
