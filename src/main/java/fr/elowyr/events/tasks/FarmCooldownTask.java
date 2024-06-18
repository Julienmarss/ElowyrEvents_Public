package fr.elowyr.events.tasks;

import fr.elowyr.events.farm.FarmManager;
import org.bukkit.scheduler.BukkitRunnable;

public class FarmCooldownTask extends BukkitRunnable {

    private final FarmManager farmManager = FarmManager.getInstance();

    @Override
    public void run() {
        if(this.farmManager.hasExpired()) {
            this.farmManager.getFarm().onStop();
            this.farmManager.resetDelay();
        }
    }
}
