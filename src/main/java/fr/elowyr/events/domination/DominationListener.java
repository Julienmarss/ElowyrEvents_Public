package fr.elowyr.events.domination;

import com.massivecraft.factions.*;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Optional;

import static com.massivecraft.factions.event.FPlayerLeaveEvent.PlayerLeaveReason.JOINOTHER;
import static com.massivecraft.factions.event.FPlayerLeaveEvent.PlayerLeaveReason.LEAVE;

public class DominationListener implements Listener {

    private final Domination domination;

    public DominationListener(Domination domination) {
        this.domination = domination;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!domination.getCurrentZones().stream().findFirst().get().getWorld().equals(player.getWorld())) {
            return;
        }
        FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
        if (!fplayer.hasFaction()) return;
        Faction faction = fplayer.getFaction();
        EntityDamageEvent lastDamageCause = player.getLastDamageCause();
        int lostPoints = this.domination.getScore(faction) - this.domination.punish(faction);

        if (lastDamageCause instanceof EntityDamageByEntityEvent) {
            this.getRealDamager((EntityDamageByEntityEvent) lastDamageCause)
                    .filter(Player.class::isInstance)
                    .map(Player.class::cast)
                    .ifPresent(value -> event.setDeathMessage(String.format(
                            "§6§lDomination §7§l• §e%s §fvient de tuer §e%s §fde la faction §b%s§f. §fLa faction à perdu §c-%d§f points",
                            value.getName(), player.getName(), faction.getTag(), lostPoints)));
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
    public void onPunch(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Arrow)) return;
        Arrow arrowDamager = (Arrow) event.getDamager();
        ProjectileSource source = arrowDamager.getShooter();
        if (!(source instanceof Player)) return;
        damager = (Player) source;
        Entity victim = event.getEntity();
        if (!(victim instanceof Player) || !victim.equals(damager) || !domination.getFlagManager().hasFlag((Player) victim)) {
            return;
        }
        event.setCancelled(true);
        damager.sendMessage("§cVous ne pouvez pas vous punch lorsque vous avez le DominationFlag !");
    }

    @EventHandler
    public void onLeave(FPlayerLeaveEvent event) {
        if (event.getReason() == LEAVE || event.getReason() == JOINOTHER) {
            event.getfPlayer().sendMessage("§cCette action est impossible pendant l'évènement");
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onDisband(FactionDisbandEvent event) {
        event.getPlayer().sendMessage("§cCette action est impossible pendant l'évènement");
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block.getType() != Material.STANDING_BANNER) {
            return;
        }
        if (domination.getFlagManager().isFlag(block.getLocation())) {
            domination.getFlagManager().captureFlag(event.getPlayer());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!domination.getFlagManager().hasFlag(player)) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current != null && current.getType() == Material.BANNER) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeathFlag(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!domination.getFlagManager().hasFlag(player)) {
            return;
        }
        event.getDrops().removeIf(item -> item != null && item.getType() == Material.BANNER);
        if (domination.getFlagManager().getFlag().getHelmet() != null) {
            player.getWorld().dropItemNaturally(player.getLocation(), domination.getFlagManager().getFlag().getHelmet());
        }
        domination.getFlagManager().spawnBanner();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        if (domination.getFlagManager().hasFlag(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack current = event.getItem();
        Player player = event.getPlayer();
        if (current.getType() != Material.GOLDEN_APPLE || current.getDurability() != 1 || !domination.getFlagManager().hasFlag(player)) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage("§cVous ne pouvez pas manger de pomme cheat lorsque vous avez le DominationFlag !");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (domination.getFlagManager().hasFlag(event.getPlayer())) {
            domination.getFlagManager().restoreHelmet();
            domination.getFlagManager().spawnBanner();
        }
    }
}
