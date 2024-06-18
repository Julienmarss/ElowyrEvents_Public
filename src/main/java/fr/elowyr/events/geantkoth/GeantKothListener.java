package fr.elowyr.events.geantkoth;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.FactionRenameEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Optional;

import static com.massivecraft.factions.event.FPlayerLeaveEvent.PlayerLeaveReason.*;

public class GeantKothListener implements Listener {
    private final GeantKoth koth;

    public GeantKothListener(GeantKoth koth) {
        this.koth = koth;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!event.getEntity().getWorld().equals(this.koth.getCurrentZone().getWorld())) return;

        FPlayer player = FPlayers.getInstance().getByPlayer(event.getEntity());
        Faction faction = player.getFaction();

        if (faction.isWilderness()) return;

        int lostPoints = this.koth.getScore(faction) - this.koth.punish(faction);

        EntityDamageEvent lastDamageCause = event.getEntity().getLastDamageCause();

        if (lastDamageCause instanceof EntityDamageByEntityEvent) {
            this.getRealDamager((EntityDamageByEntityEvent) lastDamageCause)
                    .filter(Player.class::isInstance)
                    .map(Player.class::cast)
                    .ifPresent(value -> event.setDeathMessage(String.format(
                            "§6§lKOTH Géant §7§l• §e%s §fvient de tuer §e%s §fde la faction §b%s§f. §fLa faction à perdu §c-%d§f points",
                            value.getName(), event.getEntity().getName(), faction.getTag(), lostPoints)));
        }
    }

    private Optional<Entity> getRealDamager(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
            return shooter instanceof Entity ? Optional.of((Entity) shooter) : Optional.empty();
        } else {
            return Optional.ofNullable(event.getDamager());
        }
    }

    @EventHandler
    public void onLeave(FPlayerLeaveEvent event) {
        if (event.getReason() == LEAVE || event.getReason() == JOINOTHER || event.getReason() == DISBAND) {
            event.getfPlayer().getPlayer().sendMessage("§6§lKOTH Géant §7§l• §cCette action est impossible pendant l'event");
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onDisband(FactionDisbandEvent event) {
        event.getPlayer().sendMessage("§6§lKOTH Géant §7§l• §cCette action est impossible pendant l'évènement");
        event.setCancelled(true);
    }
}