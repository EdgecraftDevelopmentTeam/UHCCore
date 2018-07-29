package smp.edgecraft.uhc.core.managers;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class UHCManager {

    public static final SettingsManager UHC_CONFIG = SettingsManager.getConfig("uhc");

    public static GameStatus GAME_STATUS = GameStatus.LOBBY;

    public static World WORLD_OVERWORLD;
    public static World WORLD_NETHER;
    public static World WORLD_END;

    public static void createUHC(String worldName) {
        WORLD_OVERWORLD = Bukkit.createWorld(new WorldCreator(worldName).environment(World.Environment.NORMAL));
        WORLD_OVERWORLD.setSpawnLocation(UHC_CONFIG.getLocation("spawn"));
        WORLD_OVERWORLD.getWorldBorder().setCenter(WORLD_OVERWORLD.getSpawnLocation());
        WORLD_OVERWORLD.getWorldBorder().setSize(UHC_CONFIG.<Integer>get("worldborder.size"));

        WORLD_NETHER = Bukkit.createWorld(new WorldCreator(worldName + "_nether").environment(World.Environment.NETHER));
        WORLD_NETHER.setSpawnLocation(UHC_CONFIG.getLocation("spawn"));
        WORLD_NETHER.getWorldBorder().setCenter(WORLD_NETHER.getSpawnLocation());
        WORLD_NETHER.getWorldBorder().setSize(UHC_CONFIG.<Integer>get("worldborder.size"));

        WORLD_END = Bukkit.createWorld(new WorldCreator(worldName + "_end").environment(World.Environment.THE_END));
        WORLD_END.setSpawnLocation(UHC_CONFIG.getLocation("spawn"));
        WORLD_END.getWorldBorder().setCenter(WORLD_END.getSpawnLocation());
        WORLD_END.getWorldBorder().setSize(UHC_CONFIG.<Integer>get("worldborder.size"));
    }

    public enum GameStatus {
        LOBBY, RUNNING, FINISHED;
    }

}
