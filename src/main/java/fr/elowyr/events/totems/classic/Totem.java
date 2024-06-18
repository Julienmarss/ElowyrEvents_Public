package fr.elowyr.events.totems.classic;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import com.massivecraft.factions.Factions;
import fr.elowyr.events.AbstractEvent;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.tasks.AntiHasteTask;
import fr.elowyr.events.tasks.AntiUseBugTask;
import fr.elowyr.events.totems.StatsCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Totem extends AbstractEvent {

    public Map<String, Integer> breakMap = new HashMap<>();
    public Map<String, Long> cooldownsMap = new HashMap<>();
    private TotemManager totemManager;
    private BukkitTask antiHaste, checkPing;
    private AntiUseBugTask antiUseBugTask;
    private LCWaypoint waypoint;

    public Totem() {
        super("totem");
    }

    @Override
    public String[] getLines() {
        return new String[]{"§fEvent: §6§lTotem", " §fCassez les §e5 §fblocs de fer.", " §fGagnez §e25 §fpoints au classement."};
    }

    @Override
    public void setup() {
        totemManager = new TotemManager(this).setup();
    }

    @Override
    public void onStart() {
        breakMap.clear();
        cooldownsMap.clear();
        this.antiHaste = new AntiHasteTask(totemManager).runTaskTimerAsynchronously(ElowyrEvents.getInstance(), 0L, 10L);
        //this.checkPing = new AntiExploitTask(totemManager).runTaskTimerAsynchronously(ElowyrEvents.getInstance(), 0L, 5L);
        sendMessage("§6§lTotem §7§l• §fLe totem a commencé.");
        totemManager.spawnBlocks();
        waypoint = new LCWaypoint("Totem",
                new Location(totemManager.getLocation().getWorld(), totemManager.getLocation().getBlockX(), totemManager.getLocation().getBlockY(),
                        totemManager.getLocation().getBlockZ()), new Color(255, 127, 0).getRGB(), true);
        for (Player player : Bukkit.getOnlinePlayers()) {
            LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
        }
    }

    @Override
    public void onStop() {
        sendMessage("§6§lTotem §7§l• §fLe totem a été arrêté.");
        this.destroy();
    }

    @Override
    public void onFinish() {
        String faction = Factions.getInstance().getFactionById(totemManager.getFactionId()).getTag();
        sendMessage("§6§lTotem §7§l• §fLe joueur §e" + totemManager.getPlayerName() + " §fde la faction §b" + faction + " §fa cassé le dernier bloc du totem.");
        sendMessage("§6§lTotem §7§l• §fLa faction §e" + faction + " §fremporte le totem.");
        sendMessage(" ");
        StatsCommand.createStats(this);
        this.getRewards().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", totemManager.getPlayerName())));
        this.destroy();
    }

    private void destroy() {
        for (int i = 0; i < 5; i++) {
            totemManager.getLocation().clone().add(0, i, 0).getBlock().setType(Material.BEDROCK);
        }
        if (totemManager.getFactionId() != null) {
            String factionName = Factions.getInstance().getFactionById(totemManager.getFactionId()).getTag();
            ElowyrEvents.getInstance().sendResult(this, Collections.singletonList(factionName));
        }
        totemManager.setFactionId(null);
        totemManager.setPlayerName(null);
        Optional.ofNullable(this.antiHaste).ifPresent(BukkitTask::cancel);
        Optional.ofNullable(this.checkPing).ifPresent(BukkitTask::cancel);
        for (Player player : Bukkit.getOnlinePlayers()) {
            LunarClientAPI.getInstance().removeWaypoint(player, waypoint);
        }
    }

    @Override
    public void onSecurize() {
        Optional.ofNullable(this.antiUseBugTask).ifPresent(BukkitRunnable::cancel);
        Bukkit.getScheduler().runTaskLater(ElowyrEvents.getInstance(), () -> {
            if (isToSecure()) {
                this.antiUseBugTask.getCuboids().forEach(cuboid -> cuboid.getMaterialWalls(Material.AIR).forEach(block -> block.setType(Material.BARRIER)));
            }
        }, 20 * 600);
        this.toSecure(true);
    }

    @Override
    public void onUnsecurize() {
        this.antiUseBugTask = new AntiUseBugTask(totemManager);
        this.antiUseBugTask.runTaskTimerAsynchronously(ElowyrEvents.getInstance(), 0L, 5L);
        this.antiUseBugTask.getCuboids().forEach(cuboid -> cuboid.getMaterialWalls(Material.BARRIER).forEach(block -> block.setType(Material.AIR)));
    }

    @Override
    public boolean isFinish() {
        return totemManager.isFinish();
    }

    public TotemManager getTotemManager() {
        return totemManager;
    }

}
