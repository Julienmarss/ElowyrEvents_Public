package fr.elowyr.events.tasks;

import fr.elowyr.events.totems.classic.TotemManager;
import fr.elowyr.events.totems.geant.TotemGeantManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class AntiHasteTask extends BukkitRunnable {

    private final Location location;

    public AntiHasteTask(Object event) {
        if (event instanceof TotemManager) {
            this.location = ((TotemManager) event).getLocation();
        } else {
            this.location = ((TotemGeantManager) event).getLocation();
        }
    }

    @Override
    public void run() {
        for (Player player : location.getWorld().getPlayers()) {
            if (!player.hasPotionEffect(PotionEffectType.FAST_DIGGING)) continue;
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        }
    }
}
