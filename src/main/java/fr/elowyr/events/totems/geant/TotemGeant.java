package fr.elowyr.events.totems.geant;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import com.massivecraft.factions.Faction;
import fr.elowyr.events.AbstractEvent;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.geantkoth.GeantKoth;
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
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class TotemGeant extends AbstractEvent {

    public final Map<String, Integer> breakMap = new HashMap<>();
    public final Map<String, Long> cooldownsMap = new HashMap<>();
    public TotemGeantManager totemManager;
    private BukkitTask timerTask, antiHaste, checkPing;
    private AntiUseBugTask antiUseBugTask;
    private LCWaypoint waypoint;

    public TotemGeant() {
        super("totemgeant");
    }

    @Override
    public String[] getLines() {
        return new String[]{"§fEvent: §6§lTotem Géant", "§f§l» §fCassez les blocs de fer les plus difficiles pour gagner.", "§f§l» §fGagnez §e15 §fpoints au classement."};
    }

    @Override
    public void setup() {
        totemManager = new TotemGeantManager(this).setup();
    }

    @Override
    public void onStart() {
        breakMap.clear();
        cooldownsMap.clear();
        totemManager.scores.clear();
        sendMessage("§6§lTotem Géant §7§l• §fL'évènement a commencé.");
        totemManager.spawnBlocks();
        totemManager.time = 1800;
        this.timerTask = Bukkit.getScheduler().runTaskTimer(ElowyrEvents.getInstance(), () -> totemManager.tick(), 0, 20);
        this.antiHaste = new AntiHasteTask(totemManager).runTaskTimerAsynchronously(ElowyrEvents.getInstance(), 0L, 10L);
        //this.checkPing = new AntiExploitTask(totemManager).runTaskTimerAsynchronously(ElowyrEvents.getInstance(), 0L, 5L);
        waypoint = new LCWaypoint("Totem Geant",
                new Location(totemManager.getLocation().getWorld(), totemManager.getLocation().getBlockX(), totemManager.getLocation().getBlockY(),
                        totemManager.getLocation().getBlockZ()), new Color(255, 127, 0).getRGB(), true);
        for (Player player : Bukkit.getOnlinePlayers()) {
            LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
        }
    }

    @Override
    public void onStop() {
        sendMessage("§6§lTotem Géant §7§l• §fL'évènement a été arrêté.");
        this.destroy();
    }

    @Override
    public void onFinish() {
        List<Map.Entry<Faction, Integer>> scores = totemManager.orderedScores().collect(Collectors.toList());
        if (scores.size() > 0) {
            sendMessage("§6§lTotem Géant §7§l• §fLe totem est terminé, le classement final est:");
            for (int i = 0; i < scores.size() && i < 3; i++) {
                this.sendMessage(GeantKoth.FINAL_TOP_FORMAT.format(new Object[]{i + 1, scores.get(i).getKey().getTag()}));
            }
        } else {
            this.sendMessage("§6§lTotem Géant §7§l• §fAucune faction n'a remporté le Totem Géant.");
        }

        this.destroy();
        StatsCommand.createStats(this);
        LinkedList<String> winners = new LinkedList<>();
        for (int i = 0; i < scores.size() && i < totemManager.rewards.size(); i++) {
            Faction faction = scores.get(i).getKey();
            winners.add(faction.getTag());
            String name = faction.getOnlinePlayers()
                    .stream()
                    .findAny()
                    .map(Player::getName)
                    .orElse(""); // if they won... They are online (skipped intensive checks)
            totemManager.rewards.get(i)
                    .stream()
                    .map(s -> s.replaceAll("%faction%", faction.getTag()))
                    .map(s -> s.replaceAll("%player%", name))
                    .forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
        ElowyrEvents.getInstance().sendResult(this, winners);
    }

    @Override
    public boolean isFinish() {
        return totemManager.isFinish();
    }

    private void destroy() {
        for (int i = 0; i < 5; i++) {
            totemManager.getLocation().clone().add(0, i, 0).getBlock().setType(Material.BEDROCK);
        }
        totemManager.setFactionId(null);
        totemManager.setPlayerName(null);
        Optional.ofNullable(this.timerTask).ifPresent(BukkitTask::cancel);
        Optional.ofNullable(this.antiHaste).ifPresent(BukkitTask::cancel);
        Optional.ofNullable(this.checkPing).ifPresent(BukkitTask::cancel);
        for (Player player : Bukkit.getOnlinePlayers()) {
            LunarClientAPI.getInstance().removeWaypoint(player, waypoint);
        }
    }

    public TotemGeantManager getTotemManager() {
        return totemManager;
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
}
