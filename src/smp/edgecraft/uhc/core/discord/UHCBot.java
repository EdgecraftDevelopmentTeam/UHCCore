package smp.edgecraft.uhc.core.discord;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import smp.edgecraft.uhc.core.managers.UHCManager;

public class UHCBot {

    public static JDA jda;
    public static Guild eventsServer;

    public static void onEnable() {
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(UHCManager.CONFIG.get("discord.token")).buildBlocking();
            eventsServer = jda.getGuildById(UHCManager.CONFIG.get("guild"));
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Game.of("UUHC"));
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

}
