package smp.edgecraft.uhc.core.managers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import smp.edgecraft.uhc.core.discord.UHCBot;
import smp.edgecraft.uhc.core.managers.UHCManager.GameStatus;
import smp.edgecraft.uhc.core.teams.UHCPlayer;
import smp.edgecraft.uhc.core.teams.UHCTeam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static smp.edgecraft.uhc.core.teams.UHCTeam.SPECTATOR;

/**
 * Handles all of the events that the UHC uses
 */
public class EventManager implements Listener {

    private static HashMap<Player, Location> respawnLocations = new HashMap<>();

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (UHCManager.GAME_STATUS == UHCManager.GameStatus.RUNNING && event.getEntity() instanceof Player &&  ((Player) event.getEntity()).getHealth() - event.getDamage() <= 0) {
            // die
            event.setCancelled(true);
            Player player = (Player) event.getEntity();
            player.setGameMode(GameMode.SPECTATOR);
            UHCManager.announce(deathMessage(player, event.getCause()));
            UHCPlayer p = UHCPlayer.get(player);
            p.setTeam(SPECTATOR);
            if (p.getDiscordMember() != null) {
                UHCBot.movePlayerToMainVC(p);
            }
            checkForEnd();
        }
    }

    public void checkForEnd(){
        List<UHCPlayer> remainingPlayers = new ArrayList<>();
        remainingPlayers.addAll(UHCPlayer.players.stream().filter(x -> x.hasTeam() && x.getTeam() != SPECTATOR).collect(Collectors.toList()));
        if (remainingPlayers.size() == 1) { // If there is now only one alive player left
            UHCManager.win(remainingPlayers.get(0).getTeam()); // They win
        } else if (remainingPlayers.size() > 0) { // Check if there is only one team left
            UHCTeam team = remainingPlayers.get(0).getTeam(); // One of the teams left
            boolean over = true;
            for (UHCPlayer uhcPlayer : remainingPlayers) {
                if (uhcPlayer.getTeam() != team) { // If a player is on a different team
                    over = false; // The game is not yet over
                    break;
                }
            }
            if (over)
                UHCManager.win(team);
        }
    }

    private String deathMessage(Player p, EntityDamageEvent.DamageCause cause) {
        String message = "";

        if(cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            message = p.getDisplayName() + " blew up";
        }
        if(cause == EntityDamageEvent.DamageCause.WITHER) {
            message = p.getDisplayName() + " withered away";
        }
        if(cause == EntityDamageEvent.DamageCause.DROWNING) {
            message = p.getDisplayName() + " drowned";
        }
        if(cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            message = p.getDisplayName() + " was slain";
        }
        if(cause == EntityDamageEvent.DamageCause.VOID) {
            message = p.getDisplayName() + " magically fell into the void";
        }
        if(cause == EntityDamageEvent.DamageCause.FALL) {
            message = p.getDisplayName() + " fell off a cliff";
        }
        if(cause == EntityDamageEvent.DamageCause.FALLING_BLOCK) {
            message = p.getDisplayName() + " was squashed by a falling block";
        }
        if(cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.HOT_FLOOR || cause == EntityDamageEvent.DamageCause.LAVA) {
            message = p.getDisplayName() + " burned to death";
        }
        if(cause == EntityDamageEvent.DamageCause.LIGHTNING) {
            message = p.getDisplayName() + " was struck by lightning";
        }
        if(cause == EntityDamageEvent.DamageCause.MAGIC) {
            message = p.getDisplayName() + " was killed by magic";
        }
        if(cause == EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
            message = p.getDisplayName() + " failed flying school";
        }
        if(cause == EntityDamageEvent.DamageCause.MELTING) {
            message = p.getDisplayName() + " melted?";
        }
        if(cause == EntityDamageEvent.DamageCause.POISON) {
            message = p.getDisplayName() + " was poisoned";
        }
        if(cause == EntityDamageEvent.DamageCause.PROJECTILE) {
            message = p.getDisplayName() + " was slain by a rogue projectile";
        }
        if(cause == EntityDamageEvent.DamageCause.STARVATION) {
            message = p.getDisplayName() + " starved to death";
        }
        if(cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
            message = p.getDisplayName() + " suffocated in a wall";
        }
        if(cause == EntityDamageEvent.DamageCause.SUICIDE) {
            message = p.getDisplayName() + " commited suicide";
        }
        if(cause == EntityDamageEvent.DamageCause.THORNS) {
            message = p.getDisplayName() + " was pricked to death";
        }

        return message;
    }
    
    /**
     * Occurs when an entity is damaged by another entity. If a player hurts another entity before the game has begun,
     * the event will be cancelled so no damage is dealt
     *
     * @param event The event to hook into
     */
    @EventHandler
    public void onEntityDamagedByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && UHCManager.GAME_STATUS != UHCManager.GameStatus.RUNNING)
            event.setCancelled(true);
    }

    /**
     * Gives the player the correct potion affects, gamemode, location when joining as well as setting up the information
     * we have on the player
     *
     * @param event The event to hook into
     */
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        UHCPlayer player = new UHCPlayer(event.getPlayer());
        if (UHCManager.CONFIG.exists("players " + player.getPlayer().getUniqueId().toString() + " discord")) {
            player.link(UHCBot.guild.getMemberById(UHCManager.CONFIG.getLong("players " + player.getPlayer().getUniqueId().toString() + " discord")));
        }
        if (UHCManager.GAME_STATUS != UHCManager.GameStatus.RUNNING) {
            event.getPlayer().teleport(UHCManager.WORLD_OVERWORLD.getSpawnLocation());
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 99999, 255, true, false));
            if (UHCManager.CONFIG.exists("players " + player.getPlayer().getUniqueId().toString() + " team")) {
                UHCTeam team = UHCTeam.get(UHCManager.CONFIG.getString("players " + player.getPlayer().getUniqueId().toString() + " team"));
                player.setTeam(team);
            }
        } else {
            player.setTeam(SPECTATOR);
            player.getPlayer().setGameMode(GameMode.SPECTATOR);
            player.getPlayer().teleport(UHCManager.WORLD_OVERWORLD.getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event){
        UHCPlayer p = UHCPlayer.get(event.getPlayer());
        p.setTeam(null);
        UHCPlayer.players.remove(p);
        checkForEnd();
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        if (UHCManager.GAME_STATUS == UHCManager.GameStatus.RUNNING && !event.getMessage().startsWith("*")) {
            UHCPlayer player = UHCPlayer.get(event.getPlayer());
            Bukkit.getOnlinePlayers().stream().filter(x -> UHCPlayer.get(x).getTeam().equals(player.getTeam())).forEach(x -> x.sendMessage(event.getPlayer().getDisplayName() + ChatColor.GREEN + " " + ChatColor.BOLD + "»" + ChatColor.GRAY + " " + ChatColor.translateAlternateColorCodes('&', event.getMessage())));
        } else {
            Bukkit.getOnlinePlayers().forEach(x -> x.sendMessage(event.getPlayer().getDisplayName() + ChatColor.DARK_GRAY + " " + ChatColor.BOLD + "»" + ChatColor.GRAY + " " + ChatColor.translateAlternateColorCodes('&', event.getMessage())));
        }
    }

    /*
    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent event) {
        if (UHCManager.GAME_STATUS == UHCManager.GameStatus.LOBBY) {
            event.setCancelled(true);
        }
    }
    */

}
