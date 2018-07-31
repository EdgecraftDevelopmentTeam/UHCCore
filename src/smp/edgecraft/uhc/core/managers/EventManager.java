package smp.edgecraft.uhc.core.managers;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventManager implements Listener {

    @EventHandler
    public void onPlayerDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player))
            return;
        Player player = (Player) event.getEntity();
        if (UHCManager.GAME_STATUS == UHCManager.GameStatus.RUNNING && player.getHealth() - event.getFinalDamage() <= 0) {
            player.setGameMode(GameMode.SPECTATOR);
            // TODO switch team
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (UHCManager.GAME_STATUS != UHCManager.GameStatus.RUNNING) event.getPlayer().setGameMode(GameMode.ADVENTURE);
    }

}
