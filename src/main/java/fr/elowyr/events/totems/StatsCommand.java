package fr.elowyr.events.totems;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import fr.elowyr.events.totems.classic.Totem;
import fr.elowyr.events.totems.geant.TotemGeant;
import fr.elowyr.events.utils.ItemBuilder;
import fr.elowyr.events.utils.api.Command;
import fr.elowyr.events.utils.api.CommandArgs;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsCommand implements Listener {

    private static Inventory statsInventory;

    public static void createStats(Object totem) {
        statsInventory = Bukkit.createInventory(null, 54, "§6§lElowyr §7▸ §eStatistiques");
        Map<String, Integer> breakMap;
        if (totem instanceof Totem) {
            breakMap = ((Totem) totem).breakMap;
        } else {
            breakMap = ((TotemGeant) totem).breakMap;
        }
        List<Map.Entry<String, Integer>> sortedScores = breakMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(54).collect(Collectors.toList());
        for (int i = 0; i < sortedScores.size(); i++) {
            FPlayer fplayer = FPlayers.getInstance().getById(sortedScores.get(i).getKey());
            statsInventory.setItem(i, new ItemBuilder(new ItemStack(Material.SKULL_ITEM, 1, (short) 3))
                    .setSkullOwner(fplayer.getName())
                    .setName(ChatColor.YELLOW + fplayer.getName())
                    .setLore((totem instanceof Totem ? "§f● §7Blocs cassés: §6" : "§f● §7Points gagnés: §6") + sortedScores.get(i).getValue(), "§f● §7Faction: §6" + fplayer.getFaction().getTag()).create());
        }
        BaseComponent[] message = new ComponentBuilder("§6§lTotem §7§l• §fAccédez aux détails du totem en cliquant sur ce message.")
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/totemstats"))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§f§l» §fClique pour voir les statistiques du totem (/totemstats).").create())).create();
        Bukkit.broadcast(message);
    }

    @Command(name = "totemstats", inGameOnly = true)
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();
        if (statsInventory == null) {
            player.sendMessage("§6§lTotem §7§l• §cIl n'y a pas de statistiques pour le moment.");
            return;
        }
        player.openInventory(statsInventory);
    }
}
