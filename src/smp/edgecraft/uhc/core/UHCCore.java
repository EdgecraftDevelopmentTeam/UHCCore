package smp.edgecraft.uhc.core;

import org.bukkit.plugin.java.JavaPlugin;
import smp.edgecraft.uhc.core.discord.UHCBot;
import smp.edgecraft.uhc.core.managers.CommandManager;
import smp.edgecraft.uhc.core.managers.EventManager;
import smp.edgecraft.uhc.core.managers.UHCManager;

/**
 * The main plugin class for the UHC
 */
public class UHCCore extends JavaPlugin {

    /**
     * The instance of the plugin
     */
    public static UHCCore instance;

    @Override
    public void onEnable() {
        instance = this;
        this.getCommand("uhc").setExecutor(CommandManager.INSTANCE); // Register the uhc command
        this.getServer().getPluginManager().registerEvents(new EventManager(), this); // Register our event handler
        UHCManager.loadConfig();
        UHCBot.onEnable(); // Enable the discord bot
        UHCManager.onEnable(); // Enable the UHC Manager
    }

    @Override
    public void onDisable() {
        UHCBot.onDisable(); // Disable the discord bot
    }
}
