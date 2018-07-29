package smp.edgecraft.uhc.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import smp.edgecraft.uhc.core.managers.CommandManager;

public class UHCCore extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getCommand("game").setExecutor(CommandManager.INSTANCE);
    }

    @Override
    public void onDisable() {
    }

    public static Plugin getPlugin() {
        return Bukkit.getServer().getPluginManager().getPlugin("MiniGameAPI");
    }
}
