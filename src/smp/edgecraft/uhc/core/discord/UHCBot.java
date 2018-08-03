package smp.edgecraft.uhc.core.discord;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import smp.edgecraft.uhc.core.managers.UHCManager;
import smp.edgecraft.uhc.core.teams.UHCPlayer;

import java.util.HashMap;
import java.util.Random;

public class UHCBot extends ListenerAdapter {

    public static JDA jda;
    public static Guild eventsServer;

    private static HashMap<String, Player> linkData = new HashMap<>();

    public static void onEnable() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(UHCManager.CONFIG.get("discord.token")).addEventListener(new UHCBot()).buildBlocking();
            eventsServer = jda.getGuildById(String.valueOf(UHCManager.CONFIG.<Long>get("discord.guild")));
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Game.of(Game.GameType.DEFAULT, "UUHC"));
            jda.getPresence().setStatus(OnlineStatus.ONLINE);
            jda.setAutoReconnect(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onDisable() {
        jda.getPresence().setStatus(OnlineStatus.OFFLINE);
        jda.shutdownNow();
    }

    public static void link(Player player) {
        for (UHCPlayer player1 : UHCManager.PLAYERS) {
            if (player1.getPlayer().equals(player) && player1.getDiscordUser() != null) {
                player.sendMessage(ChatColor.RED + "A discord account (" + player1.getDiscordUser().getName() + ") is already linked with this Minecraft account");
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
                        player1.link(event.getAuthor());
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
