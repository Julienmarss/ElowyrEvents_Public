package fr.elowyr.events.tasks;

import fr.elowyr.events.massacre.MassacreManager;
import org.bukkit.scheduler.BukkitRunnable;

public class MassacreCooldownTask extends BukkitRunnable {

    private final MassacreManager massacreManager = MassacreManager.getInstance();

    @Override
    public void run() {
        if(this.massacreManager.hasExpired()) {
            this.massacreManager.getMassacre().onStop();
            this.massacreManager.resetDelay();
        }
    }
}
