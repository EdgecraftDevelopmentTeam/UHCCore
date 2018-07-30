package smp.edgecraft.uhc.core.managers;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import smp.edgecraft.uhc.core.UHCCore;

public class UHCManager {

    public static final SettingsManager CONFIG = SettingsManager.getConfig("uhc");

    public static GameStatus GAME_STATUS = GameStatus.LOBBY;

    public static World WORLD_OVERWORLD;
    public static World WORLD_NETHER;
    public static World WORLD_END;

    public static void prepareWorld() {
        WORLD_OVERWORLD = Bukkit.getWorld(UHCManager.CONFIG.<String>get("worlds.overworld.name"));
        WORLD_NETHER = Bukkit.getWorld(UHCManager.CONFIG.<String>get("worlds.nether.name"));
        WORLD_END = Bukkit.getWorld(UHCManager.CONFIG.<String>get("worlds.end.name"));

        WORLD_OVERWORLD.setSpawnLocation(CONFIG.getLocation("worlds.overworld.spawn", WORLD_OVERWORLD));
        WORLD_OVERWORLD.getWorldBorder().setCenter(WORLD_OVERWORLD.getSpawnLocation());
        WORLD_OVERWORLD.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.size"));

        WORLD_NETHER.setSpawnLocation(CONFIG.getLocation("worlds.nether.spawn", WORLD_NETHER));
        WORLD_NETHER.getWorldBorder().setCenter(WORLD_NETHER.getSpawnLocation());
        WORLD_NETHER.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.size"));

        WORLD_END.setSpawnLocation(CONFIG.getLocation("worlds.end.spawn", WORLD_END));
        WORLD_END.getWorldBorder().setCenter(WORLD_END.getSpawnLocation());
        WORLD_END.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.size"));

        Bukkit.getScheduler().scheduleSyncDelayedTask(UHCCore.instance, () -> {
            int y = (int) WORLD_OVERWORLD.getSpawnLocation().getY() - 1;
            for (int x = (int) (WORLD_OVERWORLD.getSpawnLocation().getX() - CONFIG.<Integer>get("lobby.width") / 2); x < WORLD_OVERWORLD.getSpawnLocation().getX() + CONFIG.<Integer>get("lobby.width") / 2; x++) {
                for (int z = (int) (WORLD_OVERWORLD.getSpawnLocation().getZ() - CONFIG.<Integer>get("lobby.depth") / 2); z < WORLD_OVERWORLD.getSpawnLocation().getZ() + CONFIG.<Integer>get("lobby.depth") / 2; z++)
                    WORLD_OVERWORLD.getBlockAt(x, y, z).setType(Material.BARRIER);
            }

            // Build lobby
            for (int y2 = y; y2 < y + CONFIG.<Integer>get("lobby.height"); y2++) {
                int z = (int) WORLD_OVERWORLD.getSpawnLocation().getZ() - CONFIG.<Integer>get("lobby.depth") / 2;
                for (int x2 = (int) (WORLD_OVERWORLD.getSpawnLocation().getX() - CONFIG.<Integer>get("lobby.width") / 2); x2 < WORLD_OVERWORLD.getSpawnLocation().getX() + CONFIG.<Integer>get("lobby.width") / 2; x2++)
                    WORLD_OVERWORLD.getBlockAt(x2, y2, z).setType(Material.BARRIER);
                z = (int) WORLD_OVERWORLD.getSpawnLocation().getZ() + CONFIG.<Integer>get("lobby.depth") / 2;
                for (int x2 = (int) (WORLD_OVERWORLD.getSpawnLocation().getX() - CONFIG.<Integer>get("lobby.width") / 2); x2 < WORLD_OVERWORLD.getSpawnLocation().getX() + CONFIG.<Integer>get("lobby.width") / 2; x2++)
                    WORLD_OVERWORLD.getBlockAt(x2, y2, z).setType(Material.BARRIER);
                int x = (int) WORLD_OVERWORLD.getSpawnLocation().getX() - CONFIG.<Integer>get("lobby.width") / 2;
                for (int z2 = (int) (WORLD_OVERWORLD.getSpawnLocation().getZ() - CONFIG.<Integer>get("lobby.depth") / 2); z2 < WORLD_OVERWORLD.getSpawnLocation().getZ() + CONFIG.<Integer>get("lobby.depth") / 2; z2++)
                    WORLD_OVERWORLD.getBlockAt(x, y2, z2).setType(Material.BARRIER);
                x = (int) WORLD_OVERWORLD.getSpawnLocation().getX() + CONFIG.<Integer>get("lobby.width") / 2;
                for (int z2 = (int) (WORLD_OVERWORLD.getSpawnLocation().getZ() - CONFIG.<Integer>get("lobby.depth") / 2); z2 < WORLD_OVERWORLD.getSpawnLocation().getZ() + CONFIG.<Integer>get("lobby.depth") / 2; z2++)
                    WORLD_OVERWORLD.getBlockAt(x, y2, z2).setType(Material.BARRIER);
            }
        });
    }

    public enum GameStatus {
        LOBBY, RUNNING, FINISHED;
    }

}
