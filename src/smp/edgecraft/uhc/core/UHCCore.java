package smp.edgecraft.uhc.core;

import org.bukkit.plugin.java.JavaPlugin;
import smp.edgecraft.uhc.core.discord.UHCBot;
import smp.edgecraft.uhc.core.managers.CommandManager;
import smp.edgecraft.uhc.core.managers.EventManager;
import smp.edgecraft.uhc.core.managers.UHCManager;

public class UHCCore extends JavaPlugin {

    public static UHCCore instance;

    @Override
    public void onEnable() {
        instance = this;
        this.getCommand("uhc").setExecutor(CommandManager.INSTANCE);
        this.getServer().getPluginManager().registerEvents(new EventManager(), this);
        UHCManager.CONFIG.getConfig();
        UHCManager.prepareWorld();
        UHCBot.onEnable();
    }

    @Override
    public void onDisable() {
        UHCBot.onDisable();
    }
}
