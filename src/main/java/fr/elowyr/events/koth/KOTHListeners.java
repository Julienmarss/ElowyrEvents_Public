package fr.elowyr.events.koth;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class KOTHListeners implements Listener {

    private final KOTHManager kothManager;

    public KOTHListeners(KOTHManager kothManager) {
        this.kothManager = kothManager;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Arrow)) return;
        Arrow arrowDamager = (Arrow) event.getDamager();
        ProjectileSource source = arrowDamager.getShooter();
        if (!(source instanceof Player)) return;
        damager = (Player) source;
        Entity victim = event.getEntity();
        if (!(victim instanceof Player) || victim.equals(damager)) return;
        if (kothManager.getCurrent().isIn(victim.getLocation())) {
            damager.sendMessage("§6§lKOTH §7§l• §cVous ne pouvez pas punch ici !");
            event.setCancelled(true);
        }
    }
}
