package smp.edgecraft.uhc.core.managers;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import smp.edgecraft.uhc.core.UHCCore;
import smp.edgecraft.uhc.core.teams.UHCPlayer;
import smp.edgecraft.uhc.core.teams.UHCTeam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class UHCManager {

    public static final SettingsManager CONFIG = SettingsManager.getConfig("uhc");

    public static GameStatus GAME_STATUS = GameStatus.LOBBY;

    public static World WORLD_OVERWORLD;
    public static World WORLD_NETHER;
    public static World WORLD_END;

    public static ArrayList<UHCPlayer> PLAYERS;

    public static void prepareWorld() {
        WORLD_OVERWORLD = Bukkit.getWorld(UHCManager.CONFIG.<String>get("worlds.overworld.name"));
        WORLD_NETHER = Bukkit.getWorld(UHCManager.CONFIG.<String>get("worlds.nether.name"));
        WORLD_END = Bukkit.getWorld(UHCManager.CONFIG.<String>get("worlds.end.name"));

        if (CONFIG.<Boolean>get("worlds.prepared"))
            return;

        WORLD_OVERWORLD.setSpawnLocation(CONFIG.getLocation("worlds.overworld.spawn", WORLD_OVERWORLD));
        WORLD_OVERWORLD.getWorldBorder().setCenter(WORLD_OVERWORLD.getSpawnLocation());
        WORLD_OVERWORLD.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.size"));

        WORLD_NETHER.setSpawnLocation(CONFIG.getLocation("worlds.nether.spawn", WORLD_NETHER));
        WORLD_NETHER.getWorldBorder().setCenter(WORLD_NETHER.getSpawnLocation());
        WORLD_NETHER.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.size"));

        WORLD_END.setSpawnLocation(CONFIG.getLocation("worlds.end.spawn", WORLD_END));
        WORLD_END.getWorldBorder().setCenter(WORLD_END.getSpawnLocation());
        WORLD_END.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.size"));

        WORLD_OVERWORLD.setGameRuleValue("doDaylightCycle", "false");
        WORLD_OVERWORLD.setTime(0);

        announce(ChatColor.GREEN + "World borders created!");

        Bukkit.getScheduler().scheduleSyncDelayedTask(UHCCore.instance, () -> {
            int y = (int) WORLD_OVERWORLD.getSpawnLocation().getY() - 1;
            for (int x = (int) (WORLD_OVERWORLD.getSpawnLocation().getX() - CONFIG.<Integer>get("lobby.width") / 2); x < WORLD_OVERWORLD.getSpawnLocation().getX() + CONFIG.<Integer>get("lobby.width") / 2; x++) {
                for (int z = (int) (WORLD_OVERWORLD.getSpawnLocation().getZ() - CONFIG.<Integer>get("lobby.depth") / 2); z < WORLD_OVERWORLD.getSpawnLocation().getZ() + CONFIG.<Integer>get("lobby.depth") / 2; z++)
                    WORLD_OVERWORLD.getBlockAt(x, y, z).setType(Material.BARRIER);
            }

            // Build lobby
            for (int y2 = y; y2 < y + CONFIG.<Integer>get("lobby.height") + 1; y2++) {
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

            WORLD_OVERWORLD.getPlayers().forEach(player -> player.teleport(WORLD_OVERWORLD.getSpawnLocation()));

            announce(ChatColor.GREEN + "Lobby created!");
        });

        CONFIG.set("worlds.prepared", true);
    }

    public static void prepareTeams()
    {
        PLAYERS = new ArrayList<>();
        try {
            HashMap<UHCTeam, Integer> playersPerTeam = new HashMap<>();

            int currentTeamOrdinal = -1;

            for (Player player : Bukkit.getOnlinePlayers())
            {
                currentTeamOrdinal++;

                PLAYERS.add(new UHCPlayer(player));
                UHCTeam team = UHCTeam.values()[currentTeamOrdinal % UHCTeam.values().length];
                if (!playersPerTeam.containsKey(team))
                    playersPerTeam.put(team, 0);
                playersPerTeam.put(team, playersPerTeam.get(team) + 1);
            }

            ArrayList<UHCPlayer> unteamedPlayers = new ArrayList<>(PLAYERS);

            Random random = new Random();

            for (int i = 0; i < PLAYERS.size(); i++) {
                UHCPlayer player = unteamedPlayers.get(random.nextInt(unteamedPlayers.size() - 1));
                UHCTeam team = UHCTeam.values()[random.nextInt(UHCTeam.values().length - 1)];
                while (playersPerTeam.get(team) <= 0)
                    team = UHCTeam.values()[random.nextInt(UHCTeam.values().length - 1)];
                player.setTeam(team);
                team.getPlayers().add(player);
                playersPerTeam.put(team, playersPerTeam.get(team) - 1);
                unteamedPlayers.remove(player);
            }

            announce(ChatColor.GREEN + "Successfully created teams");
        } catch (Exception e) {
            UHCManager.announce(e);
            e.printStackTrace();
        }

    }
  
    public static void start() {
        WORLD_OVERWORLD.setGameRuleValue("doDaylightCycle", "true");
        WORLD_OVERWORLD.getPlayers().forEach(player -> player.setGameMode(GameMode.SURVIVAL));

        // Shrink border
        WORLD_OVERWORLD.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.shrink.size"), CONFIG.<Long>get("worldborder.shrink.duration"));
        WORLD_NETHER.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.shrink.size"), CONFIG.<Long>get("worldborder.shrink.duration"));
        WORLD_END.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.shrink.size"), CONFIG.<Long>get("worldborder.shrink.duration"));
    }

    public static boolean shouldBeDead(Player player, EntityDamageEvent event) {
        return !(player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING || player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) && UHCManager.GAME_STATUS == UHCManager.GameStatus.RUNNING && player.getHealth() - event.getFinalDamage() <= 0;
    }

    public static void announce(String message) {
        WORLD_OVERWORLD.getPlayers().forEach(player -> player.sendMessage(message));
        WORLD_NETHER.getPlayers().forEach(player -> player.sendMessage(message));
        WORLD_END.getPlayers().forEach(player -> player.sendMessage(message));
    }

    public static void title(String message, String subtitle) {
        WORLD_OVERWORLD.getPlayers().forEach(player -> player.sendTitle(message, subtitle, 10, 70, 20));
        WORLD_NETHER.getPlayers().forEach(player -> player.sendTitle(message, subtitle, 10, 70, 20));
        WORLD_END.getPlayers().forEach(player -> player.sendTitle(message, subtitle, 10, 70, 20));
    }

    public static void announce(Exception e) {
        announce(ChatColor.RED + e.toString());
        for (StackTraceElement element : e.getStackTrace())
            announce(ChatColor.RED + element.toString());
    }

    public enum GameStatus {
        LOBBY, RUNNING, FINISHED;
    }

}
