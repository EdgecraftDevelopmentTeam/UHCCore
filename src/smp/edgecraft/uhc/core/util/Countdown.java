package smp.edgecraft.uhc.core.util;

import org.bukkit.scheduler.BukkitRunnable;
import smp.edgecraft.uhc.core.UHCCore;

/**
 * Acts as a countdown timer
 */
public class Countdown {

    /**
     * The current time remaining
     */
    private int time;

    /**
     * Create a countdown timer using {@link #tick(int)} and {@link #finished()} to run commands whilst waiting for the timer to end.
     * @param time The amount of time we will count down from
     */
    public Countdown(int time) {
        this.time = time;
    }

    /**
     * Called every second the countdown is running
     * @param time The time remaining
     */
    public void tick(int time) {
    }

    /**
     * Called when the countdown has finished
     */
    public void finished() {

    }

    /**
     * Start the countdown
     */
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
