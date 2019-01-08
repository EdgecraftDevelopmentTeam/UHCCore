package smp.edgecraft.uhc.core.managers;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import smp.edgecraft.uhc.core.UHCCore;
import smp.edgecraft.uhc.core.discord.UHCBot;
import smp.edgecraft.uhc.core.events.UHCStartEvent;
import smp.edgecraft.uhc.core.teams.UHCPlayer;
import smp.edgecraft.uhc.core.teams.UHCTeam;
import smp.edgecraft.uhc.core.util.Coordinates;
import smp.edgecraft.uhc.core.util.Coordinates.CoordinateType;
import smp.edgecraft.uhc.core.util.Countdown;
import smp.edgecraft.uhc.core.util.DNData2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Predicates.not;
import static org.bukkit.Material.AIR;
import static smp.edgecraft.uhc.core.teams.UHCTeam.SPECTATOR;

/**
 * Handles the whole entire UHC
 */
public class UHCManager {

    /**
     * The configuration file
     */
    public static DNData2 CONFIG;

    /**
     * The state the game is in
     */
    public static GameStatus GAME_STATUS = GameStatus.LOBBY;

    /**
     * The instance of the overworld
     */
    public static World WORLD_OVERWORLD;
    /**
     * The instance of the nether
     */
    public static World WORLD_NETHER;
    /**
     * The instance of the end
     */
    public static World WORLD_END;

    public static List<Location> SPAWNS = new ArrayList<>();

    public static void loadConfig(){
        File f = new File(UHCCore.instance.getDataFolder(), "uhc.dnd");
        if(!f.exists()) loadDefault(f);
        CONFIG = new DNData2(f);
    }

    /**
     * Called when the plugin is first enabled.
     */
    public static void onEnable() {
        prepareWorld();

        UHCTeam.createSpectatorTeam();
        CONFIG.getLevels("teams").forEach(UHCTeam::new);

        for (Player player : Bukkit.getOnlinePlayers()) {
            preparePlayer(player);
        }
    }
    
    public static void loadDefault(File f){
        try {
            f.createNewFile();
            InputStream input = UHCCore.instance.getResource("uhc.dnd");
            FileOutputStream output = new FileOutputStream(f.getAbsolutePath());
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
            // Load in default uhc.yml
            input.close();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prepares the given player by linking their discord if they have already ran the link command before
     *
     * @param player The player to prepare
     */
    private static void preparePlayer(Player player) {
        UHCPlayer uhcPlayer = new UHCPlayer(player);
        UHCPlayer.players.add(uhcPlayer);

        if (UHCManager.CONFIG.exists("players " + player.getUniqueId().toString() + " team")) {
            UHCTeam team = UHCTeam.get(UHCManager.CONFIG.getString("players " + player.getUniqueId().toString() + " team"));
            uhcPlayer.setTeam(team);
        }
        if (UHCManager.CONFIG.exists("players " + player.getUniqueId().toString() + " discord")) {
            uhcPlayer.link(UHCBot.guild.getMemberById(UHCManager.CONFIG.getString("players " + player.getPlayer().getUniqueId().toString() + " discord")));
        }
    }

    /**
     * Prepares the world by creating a barrier lobby, setting the world spawns and the world border size
     */
    public static void prepareWorld() {
        // Initialise the world variables
        WORLD_OVERWORLD = Bukkit.getWorld(UHCManager.CONFIG.getString("worlds overworld name"));
        WORLD_NETHER = Bukkit.getWorld(UHCManager.CONFIG.getString("worlds nether name"));
        WORLD_END = Bukkit.getWorld(UHCManager.CONFIG.getString("worlds end name"));

        // Check if the world has already been prepared, if so, we don't need to do it again
        if (WORLD_OVERWORLD.getMetadata("prepared").size() != 0 && WORLD_OVERWORLD.getMetadata("prepared").get(0).asBoolean())
            return;

        UHCCore.instance.getLogger().info("Preparing the world!");

        // Set the overworld spawn and world border
        WORLD_OVERWORLD.setSpawnLocation(Coordinates.getLocation(WORLD_OVERWORLD, CONFIG.getString("worlds overworld spawn"), CoordinateType.ROTATION));
        WORLD_OVERWORLD.getWorldBorder().setCenter(WORLD_OVERWORLD.getSpawnLocation());
        WORLD_OVERWORLD.getWorldBorder().setSize(CONFIG.getInt("worldborder size"));

        // Set the nether spawn and world border
        WORLD_NETHER.setSpawnLocation(Coordinates.getLocation(WORLD_NETHER, CONFIG.getString("worlds nether spawn"), CoordinateType.ROTATION));
        WORLD_NETHER.getWorldBorder().setCenter(WORLD_NETHER.getSpawnLocation());
        WORLD_NETHER.getWorldBorder().setSize(CONFIG.getInt("worldborder size"));

        // Set the end spawn and world border
        WORLD_END.setSpawnLocation(Coordinates.getLocation(WORLD_END, CONFIG.getString("worlds end spawn"), CoordinateType.ROTATION));
        WORLD_END.getWorldBorder().setCenter(WORLD_END.getSpawnLocation());
        WORLD_END.getWorldBorder().setSize(CONFIG.getInt("worldborder size"));

        // Set the daylight cycle to false
        WORLD_OVERWORLD.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        WORLD_OVERWORLD.setTime(0); // Change the time
        WORLD_OVERWORLD.setGameRule(GameRule.NATURAL_REGENERATION, false);
        WORLD_OVERWORLD.setGameRule(GameRule.DO_MOB_SPAWNING, false);

        UHCCore.instance.getLogger().info("Building the lobby!");

        // Build the lobby
        Bukkit.getScheduler().scheduleSyncDelayedTask(UHCCore.instance, () -> {
            int y = (int) WORLD_OVERWORLD.getSpawnLocation().getY() - 1;
            for (int x = (int) (WORLD_OVERWORLD.getSpawnLocation().getX() - CONFIG.getInt("lobby width") / 2); x < WORLD_OVERWORLD.getSpawnLocation().getX() + CONFIG.getInt("lobby width") / 2; x++) {
                for (int z = (int) (WORLD_OVERWORLD.getSpawnLocation().getZ() - CONFIG.getInt("lobby depth") / 2); z < WORLD_OVERWORLD.getSpawnLocation().getZ() + CONFIG.getInt("lobby depth") / 2; z++)
                    WORLD_OVERWORLD.getBlockAt(x, y, z).setType(Material.BARRIER);
            }

            for (int y2 = y; y2 < y + CONFIG.getInt("lobby height") + 1; y2++) {
                int z = (int) WORLD_OVERWORLD.getSpawnLocation().getZ() - CONFIG.getInt("lobby depth") / 2;
                for (int x2 = (int) (WORLD_OVERWORLD.getSpawnLocation().getX() - CONFIG.getInt("lobby width") / 2); x2 < WORLD_OVERWORLD.getSpawnLocation().getX() + CONFIG.getInt("lobby width") / 2; x2++)
                    WORLD_OVERWORLD.getBlockAt(x2, y2, z).setType(Material.BARRIER);
                z = (int) WORLD_OVERWORLD.getSpawnLocation().getZ() + CONFIG.getInt("lobby depth") / 2;
                for (int x2 = (int) (WORLD_OVERWORLD.getSpawnLocation().getX() - CONFIG.getInt("lobby width") / 2); x2 < WORLD_OVERWORLD.getSpawnLocation().getX() + CONFIG.getInt("lobby width") / 2; x2++)
                    WORLD_OVERWORLD.getBlockAt(x2, y2, z).setType(Material.BARRIER);
                int x = (int) WORLD_OVERWORLD.getSpawnLocation().getX() - CONFIG.getInt("lobby width") / 2;
                for (int z2 = (int) (WORLD_OVERWORLD.getSpawnLocation().getZ() - CONFIG.getInt("lobby depth") / 2); z2 < WORLD_OVERWORLD.getSpawnLocation().getZ() + CONFIG.getInt("lobby depth") / 2; z2++)
                    WORLD_OVERWORLD.getBlockAt(x, y2, z2).setType(Material.BARRIER);
                x = (int) WORLD_OVERWORLD.getSpawnLocation().getX() + CONFIG.getInt("lobby width") / 2;
                for (int z2 = (int) (WORLD_OVERWORLD.getSpawnLocation().getZ() - CONFIG.getInt("lobby depth") / 2); z2 < WORLD_OVERWORLD.getSpawnLocation().getZ() + CONFIG.getInt("lobby depth") / 2; z2++)
                    WORLD_OVERWORLD.getBlockAt(x, y2, z2).setType(Material.BARRIER);
            }

            // Teleport anyone who is in the world to the spawn (probably will be no-one)
            WORLD_OVERWORLD.getPlayers().forEach(player -> player.teleport(WORLD_OVERWORLD.getSpawnLocation()));

            UHCCore.instance.getLogger().info("Built lobby!");
        });

        // Make sure we don't prepare the world again
        WORLD_OVERWORLD.setMetadata("prepared", new FixedMetadataValue(UHCCore.instance, true));
        UHCCore.instance.getLogger().info("Prepared the world!");

        for (int i = 1; i <= 8; i++) {
            Location location = Coordinates.getLocation(WORLD_OVERWORLD, CONFIG.getString("spawns " + i), CoordinateType.ROTATION);
            Biome biome = WORLD_OVERWORLD.getBiome(location.getBlockX(), location.getBlockZ());
            if (biome == Biome.COLD_OCEAN || biome == Biome.DEEP_COLD_OCEAN || biome == Biome.DEEP_FROZEN_OCEAN || biome == Biome.DEEP_LUKEWARM_OCEAN || biome == Biome.DEEP_OCEAN || biome == Biome.DEEP_WARM_OCEAN || biome == Biome.WARM_OCEAN || biome == Biome.OCEAN || location.getBlock().getType() != AIR)
                continue;
            SPAWNS.add(location);
        }

        UHCCore.instance.getLogger().info(String.format("%s valid spawns!", SPAWNS.size()));

    }

    /**
     * Prepares the teams by putting the players on the correct team
     */
    public static void prepareTeams() {
        try {
            UHCCore.instance.getLogger().info("Preparing teams!");
            Bukkit.getOnlinePlayers().stream().filter(not(UHCPlayer::hasUHCPlayer)).forEach(UHCManager::preparePlayer);

            UHCPlayer.players.stream().filter(x -> x.getTeam() == null).forEach(UHCPlayer::assertTeam);

            announce(ChatColor.GREEN + "Successfully created teams");
            UHCCore.instance.getLogger().info("Prepared the teams!");
        } catch (Exception e) {
            UHCManager.announce(e);
            e.printStackTrace();
        }

    }

    /**
     * Starts the UHC
     */
    public static void start() {
        if (UHCPlayer.players.stream().filter(not(UHCPlayer::hasTeam)).count() > 0) {
            announce(ChatColor.RED + "Not every player is on a team!");
            return;
        }
        if (UHCPlayer.players.stream().filter(x -> x.getTeam() != SPECTATOR).count() == 1) {
            announce(ChatColor.RED + "There aren't enough players!");
            return;
        }

        UHCCore.instance.getLogger().info("Starting the game!");

        // Teleport the teams
        ArrayList<Integer> chosenIndicies = new ArrayList<>();
        Random random = new Random();
        UHCTeam.teams.stream().filter(x -> x.hasPlayers() && x != SPECTATOR).forEach(x -> {
            int index = random.nextInt(SPAWNS.size()) + 1;
            while (chosenIndicies.contains(index))
                index = random.nextInt(SPAWNS.size()) + 1;
            Location location = Coordinates.getLocation(WORLD_OVERWORLD, CONFIG.getString("spawns " + index), CoordinateType.ROTATION);
            location.setY(WORLD_OVERWORLD.getHighestBlockYAt(location) + 3);
            WORLD_OVERWORLD.loadChunk(WORLD_OVERWORLD.getChunkAt(location));
            location.setPitch(90F);
            x.getPlayers().forEach(player -> player.getPlayer().teleport(location));
            chosenIndicies.add(index);
        });

        // Stop the players from moving
        UHCPlayer.players.forEach(player -> {
            if (player.getTeam() != SPECTATOR) {
                player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999, 255, true, false));
                player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 99999, 255, true, false));
                player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 99999, 255, true, false));
            }
        });

        Countdown countdown = new Countdown(3) {
            @Override
            public void tick(int time) {
                title(ChatColor.GOLD + "The game begins in", ChatColor.GREEN + String.valueOf(time));
            }

            @Override
            public void finished() {
                title(ChatColor.GOLD + "Let the games begin!", "");
                UHCPlayer.players.forEach(player -> {
                    if (player.getTeam() != SPECTATOR) {
                        player.getPlayer().removePotionEffect(PotionEffectType.SLOW);
                        player.getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
                        player.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
                    }
                });
            }
        };
        countdown.start();

        WORLD_OVERWORLD.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true); // Set the daylight cycle to true
        WORLD_OVERWORLD.setGameRule(GameRule.DO_MOB_SPAWNING, true);
        WORLD_OVERWORLD.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        WORLD_OVERWORLD.setStorm(false);
        WORLD_OVERWORLD.setThundering(false);
        UHCPlayer.players.forEach(player -> {
            if (player.getTeam() != SPECTATOR) {
                player.getPlayer().setGameMode(GameMode.SURVIVAL); // Update the gamemodes
                player.getPlayer().removePotionEffect(PotionEffectType.SATURATION); // Remove the saturation effect
                player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 255, true, false));
            } else {
                player.getPlayer().setGameMode(GameMode.SPECTATOR);
            }
        });
        GAME_STATUS = GameStatus.RUNNING;

        // Shrink border
        WORLD_OVERWORLD.getWorldBorder().setSize(CONFIG.getInt("worldborder shrink size"), CONFIG.getInt("worldborder shrink duration"));
        WORLD_NETHER.getWorldBorder().setSize(CONFIG.getInt("worldborder shrink size"), CONFIG.getInt("worldborder shrink duration"));
        WORLD_END.getWorldBorder().setSize(CONFIG.getInt("worldborder shrink size"), CONFIG.getInt("worldborder shrink duration"));

        UHCBot.movePlayersInVC(); // Move the players to the correct voice chat

        Bukkit.getServer().getPluginManager().callEvent(new UHCStartEvent());
    }

    /**
     * Called when the given team wins
     *
     * @param team The winning team
     */
    public static void win(UHCTeam team) {
        announce(ChatColor.GOLD + "The " + team.getDisplayName() + ChatColor.GOLD + " team wins!");
        Bukkit.getOnlinePlayers().forEach(x -> x.playSound(x.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1));
        GAME_STATUS = GameStatus.FINISHED;
        team.getPlayers().forEach(player -> {
            player.setTeam(SPECTATOR);
            player.getPlayer().setGameMode(GameMode.SPECTATOR);
        });

        WORLD_OVERWORLD.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    }

    /**
     * Message every player online the given message
     *
     * @param message The message to announce
     */
    public static void announce(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));
    }

    /**
     * Send a title to every online player
     *
     * @param message  The message to send
     * @param subtitle The subtitle to send
     */
    public static void title(String message, String subtitle) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(message, subtitle, 10, 70, 20));
    }

    /**
     * Announce an exception to everyone online
     *
     * @param e The exception to announce
     */
    public static void announce(Exception e) {
        announce(ChatColor.RED + e.toString());
        for (StackTraceElement element : e.getStackTrace())
            announce(ChatColor.RED + element.toString());
    }

    /**
     * The current game status
     */
    public enum GameStatus {
        LOBBY, RUNNING, FINISHED
    }

}
