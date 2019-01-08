package smp.edgecraft.uhc.core.managers;

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
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import smp.edgecraft.uhc.core.discord.UHCBot;
import smp.edgecraft.uhc.core.teams.UHCPlayer;
import smp.edgecraft.uhc.core.teams.UHCTeam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            List<UHCPlayer> remainingPlayers = new ArrayList<>();
            for (UHCPlayer uhcPlayer : UHCManager.PLAYERS) {
                if (uhcPlayer.getPlayer().equals(player)) {
                    uhcPlayer.setTeam(UHCTeam.SPECTATOR);
                    if (uhcPlayer.getDiscordMember() != null) {
                        UHCBot.movePlayerToMainVC(uhcPlayer);
                    }
                } else if (uhcPlayer.getTeam() != UHCTeam.SPECTATOR && uhcPlayer.getTeam() != UHCTeam.UNSET)
                    remainingPlayers.add(uhcPlayer);
            }

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
    }

    private String deathMessage(Player p, EntityDamageEvent.DamageCause cause) {
        String message = "";

        if(cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            message = p.getCustomName() + " blew up";
        }
        if(cause == EntityDamageEvent.DamageCause.WITHER) {
            message = p.getCustomName() + " withered away";
        }
        if(cause == EntityDamageEvent.DamageCause.DROWNING) {
            message = p.getCustomName() + " drowned";
        }
        if(cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            message = p.getCustomName() + " was slain";
        }
        if(cause == EntityDamageEvent.DamageCause.VOID) {
            message = p.getCustomName() + " magically fell into the void";
        }
        if(cause == EntityDamageEvent.DamageCause.FALL) {
            message = p.getCustomName() + " fell off a cliff";
        }
        if(cause == EntityDamageEvent.DamageCause.FALLING_BLOCK) {
            message = p.getCustomName() + " was squashed by a falling block";
        }
        if(cause == EntityDamageEvent.DamageCause.FIRE || cause == EntityDamageEvent.DamageCause.FIRE_TICK || cause == EntityDamageEvent.DamageCause.HOT_FLOOR || cause == EntityDamageEvent.DamageCause.LAVA) {
            message = p.getCustomName() + " burned to death";
        }
        if(cause == EntityDamageEvent.DamageCause.LIGHTNING) {
            message = p.getCustomName() + " was struck by lightning";
        }
        if(cause == EntityDamageEvent.DamageCause.MAGIC) {
            message = p.getCustomName() + " was killed by magic";
        }
        if(cause == EntityDamageEvent.DamageCause.FLY_INTO_WALL) {
            message = p.getCustomName() + " failed flying school";
        }
        if(cause == EntityDamageEvent.DamageCause.MELTING) {
            message = p.getCustomName() + " melted?";
        }
        if(cause == EntityDamageEvent.DamageCause.POISON) {
            message = p.getCustomName() + " was poisoned";
        }
        if(cause == EntityDamageEvent.DamageCause.PROJECTILE) {
            message = p.getCustomName() + " was slain by a rogue projectile";
        }
        if(cause == EntityDamageEvent.DamageCause.STARVATION) {
            message = p.getCustomName() + " starved to death";
        }
        if(cause == EntityDamageEvent.DamageCause.SUFFOCATION) {
            message = p.getCustomName() + " suffocated in a wall";
        }
        if(cause == EntityDamageEvent.DamageCause.SUICIDE) {
            message = p.getCustomName() + " commited suicide";
        }
        if(cause == EntityDamageEvent.DamageCause.THORNS) {
            message = p.getCustomName() + " was pricked to death";
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
        if (UHCManager.GAME_STATUS != UHCManager.GameStatus.RUNNING) {
            event.getPlayer().teleport(UHCManager.WORLD_OVERWORLD.getSpawnLocation());
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 99999, 255, true, false));
        } else event.getPlayer().setGameMode(GameMode.SURVIVAL);
        UHCPlayer player = new UHCPlayer(event.getPlayer());
        UHCManager.PLAYERS.add(player);

        if (UHCManager.CONFIG.contains("players." + player.getPlayer().getUniqueId().toString() + ".team")) {
            UHCTeam team = UHCTeam.valueOf(UHCManager.CONFIG.get("players." + player.getPlayer().getUniqueId().toString() + ".team"));
            player.setTeam(team);
        }
        if (UHCManager.CONFIG.contains("players." + player.getPlayer().getUniqueId().toString() + ".discord")) {
            player.link(UHCBot.guild.getMemberById(UHCManager.CONFIG.get("players." + player.getPlayer().getUniqueId().toString() + ".discord")));
        }
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (UHCManager.GAME_STATUS == UHCManager.GameStatus.RUNNING) {
            UHCPlayer player = UHCManager.getUHCPlayerFromPlayer(event.getPlayer());
            event.getRecipients().removeIf(p -> !UHCManager.getUHCPlayerFromPlayer(p).getTeam().equals(player.getTeam()));
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
