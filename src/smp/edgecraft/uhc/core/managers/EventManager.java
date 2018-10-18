package smp.edgecraft.uhc.core.managers;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
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

    /**
     * Handles win detection when the last player is killed
     *
     * @param event The event to hook into
     */
    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.setGameMode(GameMode.SPECTATOR);
        respawnLocations.put(player, player.getLocation());
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

    /**
     * Makes the player respawn where they died
     * @param event The event to hook into
     */
    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        if (respawnLocations.containsKey(event.getPlayer())) {
            event.setRespawnLocation(respawnLocations.get(event.getPlayer()));
            respawnLocations.remove(event.getPlayer());
        }
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

}
