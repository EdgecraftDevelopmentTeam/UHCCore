package smp.edgecraft.uhc.core.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class UHCManager {

    public static final SettingsManager UHC_CONFIG = SettingsManager.getConfig("uhc");

    private static World currentUHCWorld;

    public static void createUHC(String worldName) {
        currentUHCWorld = Bukkit.createWorld(new WorldCreator(worldName));
        currentUHCWorld.setSpawnLocation(UHC_CONFIG.getLocation("spawn"));
        currentUHCWorld.getWorldBorder().setCenter(currentUHCWorld.getSpawnLocation());
        currentUHCWorld.getWorldBorder().setSize(UHC_CONFIG.<Integer>get("worldborder.size"));
    }

}
