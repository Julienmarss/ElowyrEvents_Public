package fr.elowyr.events.ctf.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import fr.elowyr.events.ctf.CTFManager;
import fr.elowyr.events.ctf.flags.Flag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class CTFListeners implements Listener {

    private final CTFManager ctfManager;

    public CTFListeners(CTFManager ctfManager) {
        this.ctfManager = ctfManager;
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
        Player player = event.getPlayer();
        if (ctfManager.getFlag(player) != null) {
            return;
        }
        Flag flag = ctfManager.getFlag(block.getLocation());
        if (flag != null) {
            ctfManager.captureFlag(player, flag);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (ctfManager.getFlag(player) == null) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current != null && current.getType() == Material.BANNER) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Flag flag = ctfManager.getFlag(player);
        if (flag == null) {
            return;
        }
        if (flag.getHelmet() != null) {
            player.getWorld().dropItemNaturally(player.getLocation(), flag.getHelmet());
        }
        ctfManager.spawnBanner(flag);
        ctfManager.sendRespawnAlert(flag);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (ctfManager.getFlag(event.getPlayer()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Flag flag = ctfManager.getFlag(event.getPlayer());
        if (flag != null) {
            ctfManager.restoreHelmet(flag);
            ctfManager.spawnBanner(flag);
            ctfManager.sendRespawnAlert(flag);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Flag flag = ctfManager.getFlag(player);
        if (flag == null) {
            return;
        }
        Location location = event.getTo();
        if (!Board.getInstance().getFactionAt(new FLocation(location)).isWarZone()) {
            ctfManager.restoreHelmet(flag);
            ctfManager.spawnBanner(flag);
        }
        ctfManager.dropFlag(player, flag);
    }

}
