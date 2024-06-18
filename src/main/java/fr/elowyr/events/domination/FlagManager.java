package fr.elowyr.events.domination;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import fr.elowyr.events.ctf.flags.Flag;
import fr.elowyr.events.ctf.flags.FlagState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FlagManager {

    private final Domination domination;
    private final Flag flag;

    public FlagManager(Domination domination, Flag flag) {
        this.domination = domination;
        this.flag = flag;
    }

    public void restoreHelmet() {
        if (flag.getPlayerName() == null) {
            return;
        }
        Player player = Bukkit.getPlayer(flag.getPlayerName());
        ItemStack itemStack = flag.getHelmet() != null ? flag.getHelmet() : new ItemStack(Material.AIR);
        player.getInventory().setHelmet(itemStack);
        flag.setPlayerName(null);
        flag.setFactionId(null);
    }

    public void captureFlag(Player player) {
        Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
        if (!faction.isNormal()) {
            return;
        }
        flag.setFlagState(FlagState.CAPTURED);
        flag.setPlayerName(player.getName());
        flag.setFactionId(faction.getId());
        flag.getLocation().getBlock().setType(Material.AIR);
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && helmet.getType() != Material.AIR) {
            flag.setHelmet(helmet.clone());
        }
        player.getInventory().setHelmet(new ItemStack(Material.BANNER));
        domination.sendMessage("§6§lDomination §7§l• §e" + player.getName() + " §fde la faction §b" + faction.getTag() + " §fa capturé le DominationFlag !");
    }

    public void removeFlag() {
        if (flag.isFlagState(FlagState.AVAILABLE)) {
            flag.getLocation().getBlock().setType(Material.AIR);
        }
    }

    public void spawnBanner() {
        flag.setFlagState(FlagState.AVAILABLE);
        flag.setFactionId(null);
        flag.setHelmet(null);
        flag.setPlayerName(null);
        flag.getLocation().getBlock().setType(Material.STANDING_BANNER);
        Location location = flag.getLocation();
        domination.sendMessage("§6§lDomination §7§l• §fLe DominationFlag est apparu ! §c(" + location.getBlockX() + ", " + location.getBlockZ() + ")");
    }

    public boolean hasFlag(Player player) {
        return flag.getPlayerName() != null && flag.getPlayerName().equals(player.getName());
    }

    public boolean isFlag(Location location) {
        return flag.getLocation().getBlockX() == location.getBlockX()
                && flag.getLocation().getBlockY() == location.getBlockY()
                && flag.getLocation().getBlockZ() == location.getBlockZ();
    }

    public Flag getFlag() {
        return flag;
    }
}
