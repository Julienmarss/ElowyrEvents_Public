package fr.elowyr.events.totems.classic.listeners;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.totems.classic.Totem;
import fr.elowyr.events.totems.classic.TotemManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

public class TotemListeners implements Listener {

    private final Totem totem;

    public TotemListeners(Totem totem) {
        this.totem = totem;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!block.hasMetadata("IRON_BLOCK")) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        String name = player.getName();
        if (totem.cooldownsMap.containsKey(name)) {
            long timeleft = totem.cooldownsMap.get(name) - System.currentTimeMillis();
            if (timeleft > 0) {
                player.sendMessage("§6§lTotem §7§l• §cVous devez attendre " + TimeUnit.MILLISECONDS.toSeconds(timeleft) + "s avant de casser un nouveau bloc !");
                return;
            }
        }
        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() != Material.DIAMOND_SWORD) {
            player.sendMessage("§6§lTotem §7§l• §cVous devez casser le bloc avec une épée en diamant.");
            return;
        }
        Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
        if (!faction.isNormal()) {
            player.sendMessage("§6§lTotem §7§l• §fVous §cdevez§f avoir une §efaction§f.");
            return;
        }
        TotemManager totemManager = totem.getTotemManager();
        double distance = player.getLocation().distance(totemManager.getLocation());
        if (distance > 5.1) {
            double breakDistance = BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP).doubleValue();
            player.sendMessage("§6§lTotem §7§l• §cVous avez un cassé un bloc du totem en dehors de la range tolérée (" + breakDistance + ").");
            Bukkit.getOnlinePlayers()
                    .stream()
                    .filter(players -> players.hasPermission("elowyrevents.staff.use"))
                    .forEach(mods -> mods.sendMessage("§6Staff §7§l• §f " + name + " a cassé un bloc du totem en dehors de la range tolérée (" + breakDistance + ")."));
            return;
        }
        totemManager.setPlayerName(name);
        this.breakBlock(player);
        String currentFactionId = totemManager.getFactionId();
        if (currentFactionId != null && !currentFactionId.equals(faction.getId())) {
            totemManager.setFactionId(null);
            totem.sendMessage("§6§lTotem §7§l• §fLe joueur §e" + name + " §fvient de bloquer la faction §b" + Factions.getInstance().getFactionById(currentFactionId).getTag());
            totemManager.spawnBlocks();
            return;
        }
        int blockIndice = this.getBlockIndice(block);
        block.setType(Material.AIR);
        totemManager.getBlocksPresent()[blockIndice] = false;
        totemManager.playersCache.put(blockIndice, name);
        if (!ElowyrEvents.getInstance().getEvents().finishEvent(totem)) {
            totemManager.setFactionId(faction.getId());
            totem.sendMessage("§6§lTotem §7§l• §fLe joueur §e" + name + " §fde la faction §b" + faction.getTag() + " §fvient de casser un bloc du totem.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().getName().equals(totem.getTotemManager().getLocation().getWorld().getName())) {
            return;
        }
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_AIR) {
            player.updateInventory();
        } else if (action == Action.LEFT_CLICK_BLOCK) {
            if (event.getClickedBlock().hasMetadata("IRON_BLOCK")) {
                player.updateInventory();
            }
        }
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
        Location totemLocation = totem.getTotemManager().getLocation();
        if (victim.getWorld().equals(totemLocation.getWorld()) && totemLocation.distance(victim.getLocation()) <= 20.0) {
            event.setCancelled(true);
            damager.sendMessage("§6§lTotem §7§l• §cVous ne pouvez pas punch ici !");
        }
    }

    private void breakBlock(Player player) {
        String playerId = FPlayers.getInstance().getByPlayer(player).getId();
        totem.breakMap.put(playerId, totem.breakMap.containsKey(playerId) ? totem.breakMap.get(playerId) + 1 : 1);
        if (!player.isOp()) {
            totem.cooldownsMap.put(player.getName(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20));
        }
    }

    private int getBlockIndice(final Block block) {
        int blockIndice = 0;
        Location totemLoc = totem.getTotemManager().getLocation();
        for (int i = 0; i < 5; i++) {
            Block totemBlock = totemLoc.clone().add(0, i, 0).getBlock();
            if (totemBlock.equals(block)) {
                blockIndice = i;
                break;
            }
        }
        return blockIndice;
    }
}