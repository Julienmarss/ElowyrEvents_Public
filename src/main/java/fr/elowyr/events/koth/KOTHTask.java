package fr.elowyr.events.koth;

import org.bukkit.scheduler.BukkitRunnable;

public class KOTHTask extends BukkitRunnable {

    private final KOTHManager kothManager;
    private final int ticks;
    private int currentTicks;

    public KOTHTask(KOTHManager kothManager, int ticks) {
        this.kothManager = kothManager;
        this.ticks = ticks;
    }

    @Override
    public void run() {
        currentTicks += ticks;
        if (currentTicks >= 20) {
            currentTicks = 0;
            kothManager.makeTime();
        }
        kothManager.makeCaptureArea();
    }
}
