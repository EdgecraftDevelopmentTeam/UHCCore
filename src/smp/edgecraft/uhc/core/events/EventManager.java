package smp.edgecraft.uhc.core.events;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import smp.edgecraft.uhc.core.managers.UHCManager;

public class EventManager implements Listener {

    @EventHandler
    public void onPortalEnter(PlayerPortalEvent event) {
        switch (event.getCause()) {
            case END_PORTAL:
                if (event.getFrom().getWorld() == UHCManager.currentUHCEnd) {
                    event.setTo(event.getPlayer().getBedSpawnLocation());
                }
                else {
                    event.setTo(UHCManager.currentUHCEnd.getSpawnLocation());
                }
                break;
            case NETHER_PORTAL:
                if (event.getFrom().getWorld() == UHCManager.currentUHCNether) {
                    Location location = new Location(UHCManager.currentUHCOverworld, event.getPlayer().getLocation().getX() * 8, event.getPlayer().getLocation().getY(), event.getPlayer().getLocation().getZ() * 8);
                    event.getPortalTravelAgent().findOrCreate(location);
                    event.setTo(location);
                }
                else {
                    Location location = new Location(UHCManager.currentUHCNether, event.getPlayer().getLocation().getX() / 8, event.getPlayer().getLocation().getY(), event.getPlayer().getLocation().getZ() / 8);
                    event.getPortalTravelAgent().findOrCreate(location);
                    event.setTo(location);
                }
                break;
        }
    }

}
