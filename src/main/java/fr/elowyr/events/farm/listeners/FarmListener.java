package fr.elowyr.events.farm.listeners;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import fr.elowyr.events.utils.Utils;
import fr.elowyr.events.farm.Farm;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class FarmListener implements Listener {

    private final Farm farm;

    public FarmListener(Farm farm) {
        this.farm = farm;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (this.farm.isStarted()) {
            if (!event.isCancelled()) {
                Player player = event.getPlayer();
                FPlayer fPlayer = FPlayers.getInstance().getByPlayer(player);
                Faction island = fPlayer.getFaction();
                if (island != null) {
                    Block block = event.getBlock();
                    if (Utils.isPlant(block)) {
                        int requiredMeta = Utils.getRequiredMeta(block);
                        if (block.getData() >= requiredMeta) {
                            this.farm.incrementPoints(island, block.getDrops().size());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCommandFarming(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().equalsIgnoreCase("/farming")) {
            if (!farm.isStarted()) {
                event.getPlayer().sendMessage(Utils.color("§4§l✘ &7◆ &fLe &6Tournoi Agricole &fn'est pas en &ccours&f. &7(/events)"));
                event.setCancelled(true);
            }
        }
    }
}
