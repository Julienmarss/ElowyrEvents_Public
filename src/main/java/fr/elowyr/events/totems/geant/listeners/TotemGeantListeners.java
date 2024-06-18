package fr.elowyr.events.totems.geant.listeners;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.elowyr.events.totems.geant.TotemGeant;
import fr.elowyr.events.totems.geant.TotemGeantManager;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

public class TotemGeantListeners implements Listener {

    private final TotemGeant totem;

    public TotemGeantListeners(TotemGeant totem) {
        this.totem = totem;
    }

    @SuppressWarnings("ConstantConditions")
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
                player.sendMessage("§6§lTotem Géant §7§l• §cVous devez attendre " + TimeUnit.MILLISECONDS.toSeconds(timeleft) + "s avant de casser un nouveau bloc !");
                return;
            }
        }
        ItemStack item = player.getItemInHand();
        if (item == null || item.getType() != Material.DIAMOND_SWORD) {
            player.sendMessage("§6§lTotem Géant §7§l• §cVous devez casser le bloc avec une épée en diamant.");
            return;
        }
        Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
        if (!faction.isNormal()) {
            player.sendMessage("§6§lTotem Géant §7§l• §fVous§c devez§f avoir une §efaction§f.");
            return;
        }
        TotemGeantManager totemManager = totem.getTotemManager();
        double distance = player.getLocation().distance(totemManager.getLocation());
        if (distance > 5.1) {
            double breakDistance = BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP).doubleValue();
            player.sendMessage("§6§lTotem Géant §7§l• §cVous avez un cassé un bloc du totem en dehors de la range tolérée (" + breakDistance + ").");
            Bukkit.getOnlinePlayers().stream().filter(players -> players.hasPermission("staff.use"))
                    .forEach(mods -> mods.sendMessage("§6§lModération §7§l• §e" + name + " §fa cassé un bloc du totem en dehors de la range tolérée (" + breakDistance + ")."));
            return;
        }
        String currentFactionId = totemManager.getFactionId();
        int blockIndice = this.getBlockIndice(block);
        totemManager.scores.compute(faction, (k, v) -> v == null ? totemManager.values[blockIndice] : v + totemManager.values[blockIndice]);
        this.breakBlock(player, totemManager.values[blockIndice]);
        if (currentFactionId != null && !currentFactionId.equals(faction.getId())) {
            totemManager.setFactionId(null);
            totemManager.spawnBlocks();
            totem.sendMessage("§6§lTotem Géant §7§l• §fLe joueur §e" + name + " §fvient de bloquer la faction §b" + Factions.getInstance().getFactionById(currentFactionId).getTag());
            return;
        }
        totem.sendMessage("§6§lTotem Géant §7§l• §fLe joueur §e" + name + " §fde la faction §b" + faction.getTag() + " §fvient de casser un bloc du totem.");
        if (totemManager.getBlocks() == 1) {
            totemManager.scores.compute(faction, (k, v) -> v + 10);
            totemManager.setFactionId(null);
            totemManager.spawnBlocks();
            totem.sendMessage("§6§lTotem Géant §7§l• §fLa faction §e" + faction.getTag() + " §fa oneshot le totem ! Elle remporte §b10 points §fsupplémentaires !");
            return;
        }
        block.setType(Material.AIR);
        totemManager.setFactionId(faction.getId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.getWorld().equals(totem.getTotemManager().getLocation().getWorld())) {
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
        if (victim.getWorld().equals(totem.getTotemManager().getLocation().getWorld()) && totem.getTotemManager().getLocation().distance(victim.getLocation()) <= 20.0) {
            damager.sendMessage("§6§lTotem Géant §7§l• §cVous ne pouvez pas punch ici !");
            event.setCancelled(true);
        }
    }

    private void breakBlock(Player player, int value) {
        String playerId = FPlayers.getInstance().getByPlayer(player).getId();
        totem.breakMap.put(playerId, totem.breakMap.containsKey(playerId) ? totem.breakMap.get(playerId) + value : value);
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