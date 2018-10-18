package smp.edgecraft.uhc.core.util;

import org.bukkit.scheduler.BukkitRunnable;
import smp.edgecraft.uhc.core.UHCCore;

public class Countdown {

    private int time;

    public Countdown(int time) {
        this.time = time;
    }

    public void tick(int time) {
    }

    public void finished() {

    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (time <= 0) {
                    finished();
                    cancel();
                    return;
                }
                tick(time);
                time--;
            }
        }.runTaskTimer(UHCCore.instance, 0L, 20L);
    }
}
