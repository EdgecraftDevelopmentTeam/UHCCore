package smp.edgecraft.uhc.core;

import org.bukkit.plugin.java.JavaPlugin;
import smp.edgecraft.uhc.core.managers.CommandManager;

public class UHCCore extends JavaPlugin {

    public static UHCCore instance;

    @Override
    public void onEnable() {
        instance = this;
        this.getCommand("uhc").setExecutor(CommandManager.INSTANCE);
    }

    @Override
    public void onDisable() {
    }
}
