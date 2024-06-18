package fr.elowyr.events.koth;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import com.massivecraft.factions.Factions;
import fr.elowyr.events.AbstractEvent;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.tasks.AntiUseBugTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.util.Collections;
import java.util.Optional;

public class KOTH extends AbstractEvent {

    public KOTHManager kothManager;
    private BukkitTask kothTask;
    private AntiUseBugTask antiUseBugTask;
    private LCWaypoint waypoint;

    public KOTH() {
        super("KOTH");
    }

    @Override
    public String[] getLines() {
        return new String[]{
                "§fEvent: §6§lKOTH", " §fVous devez capturer la zone pendant §eune durée indiquée§f.",
                " §fGagnez §e10 §fpoints au classement."};
    }

    @Override
    public void setup() {
        kothManager = new KOTHManager(this).setup();
    }

    @Override
    public void onStart() {
        sendMessage("§6§lKOTH §7§l• §fL'évènement a commencé.");
        kothManager.reset();
        this.kothTask = new KOTHTask(kothManager, 10).runTaskTimerAsynchronously(ElowyrEvents.getInstance(), 0L, 10L);
        waypoint = new LCWaypoint("Koth",
                new Location(kothManager.getCurrent().getWorld(), kothManager.getCurrent().getxMax() - 1, kothManager.getCurrent().getyMin(),
                        kothManager.getCurrent().getzMax() - 1), new Color(255, 127, 0).getRGB(), true);
        for (Player player : Bukkit.getOnlinePlayers()) {
            LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
        }
    }

    @Override
    public void onStop() {
        this.cancel();
        sendMessage("§6§lKOTH §7§l• §fL'évènement a été arrêté.");
    }

    @Override
    public void onFinish() {
        Bukkit.getScheduler().runTask(ElowyrEvents.getInstance(), () -> {
            String faction = Factions.getInstance().getFactionById(kothManager.getFactionId()).getTag();
            getRewards().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", kothManager.getPlayerName())));
            this.sendMessage("§6§lKOTH §7§l• §fLa faction §e" + faction + " §fremporte l'évènement.");
            this.cancel();
            ElowyrEvents.getInstance().sendResult(this, Collections.singletonList(faction));
        });
    }

    @Override
    public boolean isFinish() {
        return kothManager.getTime() <= 0;
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
        this.antiUseBugTask = new AntiUseBugTask(kothManager);
        this.antiUseBugTask.runTaskTimerAsynchronously(ElowyrEvents.getInstance(), 0L, 5L);
        this.antiUseBugTask.getCuboids().forEach(cuboid -> cuboid.getMaterialWalls(Material.BARRIER).forEach(block -> block.setType(Material.AIR)));
    }

    private void cancel() {
        Optional.ofNullable(this.kothTask).ifPresent(BukkitTask::cancel);
        for (Player player : Bukkit.getOnlinePlayers()) {
            LunarClientAPI.getInstance().removeWaypoint(player, waypoint);
        }
       // this.onSecurize();
    }
}