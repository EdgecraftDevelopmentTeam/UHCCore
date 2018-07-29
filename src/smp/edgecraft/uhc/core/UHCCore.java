package smp.edgecraft.uhc.core;

import org.bukkit.plugin.java.JavaPlugin;
import smp.edgecraft.uhc.core.events.EventManager;
import smp.edgecraft.uhc.core.managers.CommandManager;

public class UHCCore extends JavaPlugin {

    public static UHCCore instance;

    @Override
    public void onEnable() {
        instance = this;
        this.getCommand("game").setExecutor(CommandManager.INSTANCE);
        getServer().getPluginManager().registerEvents(new EventManager(), this);
    }

    @Override
    public void onDisable() {
    }
}
