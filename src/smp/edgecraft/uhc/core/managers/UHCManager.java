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
import smp.edgecraft.uhc.core.util.Countdown;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Handles the whole entire UHC
 */
public class UHCManager {

    /**
     * The configuration file
     */
    public static final SettingsManager CONFIG = SettingsManager.getConfig("uhc");

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

    /**
     * All of the online players
     */
    public static ArrayList<UHCPlayer> PLAYERS = new ArrayList<>();
    /**
     * All of the active teams
     */
    public static ArrayList<UHCTeam> TEAMS = new ArrayList<>();

    /**
     * Called when the plugin is first enabled.
     */
    public static void onEnable() {
        prepareWorld();

        for (Player player : Bukkit.getOnlinePlayers()) {
            preparePlayer(player);
        }
    }

    /**
     * Prepares the given player by linking their discord if they have already ran the link command before
     *
     * @param player The player to prepare
     */
    private static void preparePlayer(Player player) {
        UHCPlayer uhcPlayer = new UHCPlayer(player);
        UHCManager.PLAYERS.add(uhcPlayer);

        if (UHCManager.CONFIG.contains("players." + player.getUniqueId().toString() + ".team")) {
            UHCTeam team = UHCTeam.valueOf(UHCManager.CONFIG.get("players." + player.getUniqueId().toString() + ".team"));
            uhcPlayer.setTeam(team);
        }
        if (UHCManager.CONFIG.contains("players." + player.getUniqueId().toString() + ".discord")) {
            uhcPlayer.link(UHCBot.guild.getMemberById(UHCManager.CONFIG.get("players." + player.getPlayer().getUniqueId().toString() + ".discord")));
        }
    }

    /**
     * Prepares the world by creating a barrier lobby, setting the world spawns and the world border size
     */
    public static void prepareWorld() {
        // Initialise the world variables
        WORLD_OVERWORLD = Bukkit.getWorld(UHCManager.CONFIG.<String>get("worlds.overworld.name"));
        WORLD_NETHER = Bukkit.getWorld(UHCManager.CONFIG.<String>get("worlds.nether.name"));
        WORLD_END = Bukkit.getWorld(UHCManager.CONFIG.<String>get("worlds.end.name"));

        // Check if the world has already been prepared, if so, we don't need to do it again
        if (WORLD_OVERWORLD.getMetadata("prepared").size() != 0 && WORLD_OVERWORLD.getMetadata("prepared").get(0).asBoolean())
            return;

        UHCCore.instance.getLogger().info("Preparing the world!");

        // Set the overworld spawn and world border
        WORLD_OVERWORLD.setSpawnLocation(CONFIG.getLocation("worlds.overworld.spawn", WORLD_OVERWORLD));
        WORLD_OVERWORLD.getWorldBorder().setCenter(WORLD_OVERWORLD.getSpawnLocation());
        WORLD_OVERWORLD.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.size"));

        // Set the nether spawn and world border
        WORLD_NETHER.setSpawnLocation(CONFIG.getLocation("worlds.nether.spawn", WORLD_NETHER));
        WORLD_NETHER.getWorldBorder().setCenter(WORLD_NETHER.getSpawnLocation());
        WORLD_NETHER.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.size"));

        // Set the end spawn and world border
        WORLD_END.setSpawnLocation(CONFIG.getLocation("worlds.end.spawn", WORLD_END));
        WORLD_END.getWorldBorder().setCenter(WORLD_END.getSpawnLocation());
        WORLD_END.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.size"));

        // Set the daylight cycle to false
        WORLD_OVERWORLD.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        WORLD_OVERWORLD.setTime(0); // Change the time
        WORLD_OVERWORLD.setGameRule(GameRule.NATURAL_REGENERATION, false);
        WORLD_OVERWORLD.setGameRule(GameRule.DO_MOB_SPAWNING, false);

        UHCCore.instance.getLogger().info("Building the lobby!");

        // Build the lobby
        Bukkit.getScheduler().scheduleSyncDelayedTask(UHCCore.instance, () -> {
            int y = (int) WORLD_OVERWORLD.getSpawnLocation().getY() - 1;
            for (int x = (int) (WORLD_OVERWORLD.getSpawnLocation().getX() - CONFIG.<Integer>get("lobby.width") / 2); x < WORLD_OVERWORLD.getSpawnLocation().getX() + CONFIG.<Integer>get("lobby.width") / 2; x++) {
                for (int z = (int) (WORLD_OVERWORLD.getSpawnLocation().getZ() - CONFIG.<Integer>get("lobby.depth") / 2); z < WORLD_OVERWORLD.getSpawnLocation().getZ() + CONFIG.<Integer>get("lobby.depth") / 2; z++)
                    WORLD_OVERWORLD.getBlockAt(x, y, z).setType(Material.BARRIER);
            }

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

            // Teleport anyone who is in the world to the spawn (probably will be no-one)
            WORLD_OVERWORLD.getPlayers().forEach(player -> player.teleport(WORLD_OVERWORLD.getSpawnLocation()));

            UHCCore.instance.getLogger().info("Built lobby!");
        });

        // Make sure we don't prepare the world again
        WORLD_OVERWORLD.setMetadata("prepared", new FixedMetadataValue(UHCCore.instance, true));
        UHCCore.instance.getLogger().info("Prepared the world!");

        for (int i = 1; i <= 8; i++) {
            Location location = CONFIG.getLocation("spawns." + i, WORLD_OVERWORLD);
            Biome biome = WORLD_OVERWORLD.getBiome(location.getBlockX(), location.getBlockZ());
            if (biome == Biome.COLD_OCEAN || biome == Biome.DEEP_COLD_OCEAN || biome == Biome.DEEP_FROZEN_OCEAN || biome == Biome.DEEP_LUKEWARM_OCEAN || biome == Biome.DEEP_OCEAN || biome == Biome.DEEP_WARM_OCEAN || biome == Biome.WARM_OCEAN || biome == Biome.OCEAN)
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
            HashMap<UHCTeam, Integer> playersPerTeam = new HashMap<>(); // Stores how many players should be on each team

            int currentTeamOrdinal = 0;

            // Initialise the hashmap
            for (int teamOrdinal = 0; playersPerTeam.size() < SPAWNS.size() && teamOrdinal < UHCTeam.values().length; teamOrdinal++) {
                UHCTeam team = UHCTeam.values()[teamOrdinal];
                if (team != UHCTeam.UNSET && team != UHCTeam.SPECTATOR)
                    playersPerTeam.put(team, 0);
            }
            for (UHCTeam team : UHCTeam.values()) {
                if (team != UHCTeam.UNSET && team != UHCTeam.SPECTATOR)
                    playersPerTeam.put(team, 0);
            }

            // Setup the players
            for (Player player : Bukkit.getOnlinePlayers()) {
                boolean found = false;
                // Check if the player already has been setup
                for (UHCPlayer uhcPlayer : PLAYERS) {
                    if (uhcPlayer.getPlayer().equals(player)) {
                        found = true;
                        break;
                    }
                }
                if (!found) { // If not, set the player up
                    preparePlayer(player);
                }
            }

            // Calculate the number of players that should be on each team
            for (UHCPlayer player : PLAYERS) {
                if (player.getTeam() == UHCTeam.SPECTATOR)
                    continue;
                currentTeamOrdinal++;
                if (currentTeamOrdinal >= UHCTeam.values().length - 1) // Ignore the spectator team
                    currentTeamOrdinal = 1;
                UHCTeam team = UHCTeam.values()[currentTeamOrdinal];
                playersPerTeam.put(team, playersPerTeam.get(team) + 1);
            }

            // Runs through the players who have no team
            ArrayList<UHCPlayer> unteamedPlayers = new ArrayList<>();
            for (UHCPlayer player : PLAYERS)
                if (player.getTeam() != UHCTeam.SPECTATOR)
                    unteamedPlayers.add(player);

            Random random = new Random();

            currentTeamOrdinal = 1;

            // Give each player a team
            for (int i = 0; i < PLAYERS.size(); i++) {
                if (unteamedPlayers.size() == 0)
                    break;
                // Select a random player who hasn't had a team set
                UHCPlayer player = unteamedPlayers.get(random.nextInt(unteamedPlayers.size()));
                if (player.getTeam() == UHCTeam.SPECTATOR) // Skip any spectators
                    continue;
                int timesRan = 0;
                UHCTeam team = UHCTeam.values()[currentTeamOrdinal];
                while (team.getPlayers().size() == playersPerTeam.get(team)) { // While the select team is full
                    currentTeamOrdinal++;
                    if (currentTeamOrdinal >= UHCTeam.values().length - 1) // Ignore the spectator team
                        currentTeamOrdinal = 1;
                    timesRan++;
                    team = UHCTeam.values()[currentTeamOrdinal];
                    if (timesRan == UHCTeam.values().length - 2)
                        break;
                }
                if (currentTeamOrdinal > UHCTeam.values().length)
                    currentTeamOrdinal = 1;
                player.setTeam(team); // Change the team
                unteamedPlayers.remove(player);
            }

            for (UHCTeam team : UHCTeam.values())
                if (team.isActive())
                    TEAMS.add(team); // Add the active teams

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
        if (UHCTeam.UNSET.getPlayers().size() > 0) {
            announce(ChatColor.RED + "Not every player is on a team!");
            return;
        }
        if (PLAYERS.size() == 1) {
            announce(ChatColor.RED + "There aren't enough players!");
            return;
        }

        UHCCore.instance.getLogger().info("Starting the game!");

        // Teleport the teams
        ArrayList<Integer> chosenIndicies = new ArrayList<>();
        Random random = new Random();
        for (UHCTeam team : TEAMS) {
            if (team == UHCTeam.SPECTATOR)
                continue;
            int index = random.nextInt(SPAWNS.size()) + 1;
            while (chosenIndicies.contains(index))
                index = random.nextInt(SPAWNS.size()) + 1;
            Location location = CONFIG.getLocation("spawns." + index, WORLD_OVERWORLD);
            location.setY(WORLD_OVERWORLD.getHighestBlockYAt(location) + 3);
            WORLD_OVERWORLD.loadChunk(WORLD_OVERWORLD.getChunkAt(location));
            location.setPitch(90F);
            team.getPlayers().forEach(player -> player.getPlayer().teleport(location));
        }

        // Stop the players from moving
        PLAYERS.forEach(player -> {
            if (player.getTeam() != UHCTeam.SPECTATOR) {
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
                PLAYERS.forEach(player -> {
                    if (player.getTeam() != UHCTeam.SPECTATOR) {
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
        PLAYERS.forEach(player -> {
            if (player.getTeam() != UHCTeam.SPECTATOR) {
                player.getPlayer().setGameMode(GameMode.SURVIVAL); // Update the gamemodes
                player.getPlayer().removePotionEffect(PotionEffectType.SATURATION); // Remove the saturation effect
                player.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 255, true, false));
            } else {
                player.getPlayer().setGameMode(GameMode.SPECTATOR);
            }
        });
        GAME_STATUS = GameStatus.RUNNING;

        // Shrink border
        WORLD_OVERWORLD.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.shrink.size"), CONFIG.<Integer>get("worldborder.shrink.duration"));
        WORLD_NETHER.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.shrink.size"), CONFIG.<Integer>get("worldborder.shrink.duration"));
        WORLD_END.getWorldBorder().setSize(CONFIG.<Integer>get("worldborder.shrink.size"), CONFIG.<Integer>get("worldborder.shrink.duration"));

        UHCBot.movePlayersInVC(); // Move the players to the correct voice chat

        Bukkit.getServer().getPluginManager().callEvent(new UHCStartEvent());
    }

    /**
     * Called when the given team wins
     *
     * @param team The winning team
     */
    public static void win(UHCTeam team) {
        announce(ChatColor.GOLD + "The " + ChatColor.BOLD + team.getTeamColor() + team.name().toLowerCase() + ChatColor.RESET + ChatColor.GOLD + " team wins!");
        Bukkit.getOnlinePlayers().forEach(x -> x.playSound(x.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1));
        // Some win particle / sound?
        GAME_STATUS = GameStatus.FINISHED;
        team.getPlayers().forEach(player -> {
            player.setTeam(UHCTeam.SPECTATOR);
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

    public static UHCPlayer getUHCPlayerFromPlayer(Player player) {
        for (UHCPlayer uhcPlayer : PLAYERS)
            if (uhcPlayer.getPlayer().equals(player))
                return uhcPlayer;
        return null;
    }

    /**
     * The current game status
     */
    public enum GameStatus {
        LOBBY, RUNNING, FINISHED
    }

}
