package smp.edgecraft.uhc.core.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class UHCManager {

    public static final SettingsManager UHC_CONFIG = SettingsManager.getConfig("uhc");

    public static World currentUHCOverworld;
    public static World currentUHCNether;
    public static World currentUHCEnd;

    public static void createUHC(String worldName) {
        currentUHCOverworld = Bukkit.createWorld(new WorldCreator(worldName).environment(World.Environment.NORMAL));
        currentUHCOverworld.setSpawnLocation(UHC_CONFIG.getLocation("spawn"));
        currentUHCOverworld.getWorldBorder().setCenter(currentUHCOverworld.getSpawnLocation());
        currentUHCOverworld.getWorldBorder().setSize(UHC_CONFIG.<Integer>get("worldborder.size"));

        currentUHCNether = Bukkit.createWorld(new WorldCreator(worldName + "_nether").environment(World.Environment.NETHER));
        currentUHCNether.setSpawnLocation(UHC_CONFIG.getLocation("spawn"));
        currentUHCNether.getWorldBorder().setCenter(currentUHCNether.getSpawnLocation());
        currentUHCNether.getWorldBorder().setSize(UHC_CONFIG.<Integer>get("worldborder.size"));

        currentUHCEnd = Bukkit.createWorld(new WorldCreator(worldName + "_end").environment(World.Environment.THE_END));
        currentUHCEnd.setSpawnLocation(UHC_CONFIG.getLocation("spawn"));
        currentUHCEnd.getWorldBorder().setCenter(currentUHCEnd.getSpawnLocation());
        currentUHCEnd.getWorldBorder().setSize(UHC_CONFIG.<Integer>get("worldborder.size"));
    }

}
