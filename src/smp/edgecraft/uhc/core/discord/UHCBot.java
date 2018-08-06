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

public class UHCBot extends ListenerAdapter {

    public static JDA jda;
    public static Guild guild;
    public static GuildController guildController;

    public static VoiceChannel mainChannel;
    public static VoiceChannel blueChannel;
    public static VoiceChannel redChannel;
    public static VoiceChannel yellowChannel;
    public static VoiceChannel greenChannel;
    public static VoiceChannel pinkChannel;

    private static HashMap<String, Player> linkData = new HashMap<>();

    public static void onEnable() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(UHCManager.CONFIG.get("discord.token")).addEventListener(new UHCBot()).buildBlocking();
            guild = jda.getGuildById(String.valueOf(UHCManager.CONFIG.<Long>get("discord.guild")));
            guildController = guild.getController();
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Game.of(Game.GameType.DEFAULT, "UUHC"));
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.setAutoReconnect(true);
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

    public static void onDisable() {
        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
        jda.shutdownNow();
    }

    public static void movePlayersInVC() {
        for (UHCPlayer player : UHCManager.PLAYERS) {
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

    public static void movePlayerToMainVC(UHCPlayer player) {
        guildController.moveVoiceMember(player.getDiscordMember(), mainChannel).queue();
    }

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

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentDisplay().startsWith("!link ")) {
            if (linkData.containsKey(event.getMessage().getContentDisplay().substring(6))) {
                Player player = linkData.get(event.getMessage().getContentDisplay().substring(6));
                for (UHCPlayer player1 : UHCManager.PLAYERS) {
                    if (player1.getPlayer().equals(player)) {
                        UHCManager.CONFIG.set("players." + player.getUniqueId().toString() + ".discord", event.getAuthor().getId().toString());
                        player1.link(event.getMember());
                        player.sendMessage(ChatColor.GREEN + "Sucessfully linked discord user (" + event.getAuthor().getName() + ") with your Minecraft account!");
                        event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage(event.getAuthor().getAsMention() + " you have sucessfully linked your Minecraft account (" + player.getName() + ") with your discord account!").queue());
                        linkData.remove(event.getMessage().getContentDisplay().substring(6));
                        break;
                    }
                }
            }
        }
    }
}
