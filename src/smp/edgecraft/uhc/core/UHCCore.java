package smp.edgecraft.uhc.core;

import org.bukkit.plugin.java.JavaPlugin;
import smp.edgecraft.uhc.core.managers.CommandManager;
import smp.edgecraft.uhc.core.managers.EventManager;

public class UHCCore extends JavaPlugin {

    public static UHCCore instance;

    @Override
    public void onEnable() {
        instance = this;
        this.getCommand("uhc").setExecutor(CommandManager.INSTANCE);
        this.getServer().getPluginManager().registerEvents(new EventManager(), this);
    }

    @Override
    public void onDisable() {
    }
}
