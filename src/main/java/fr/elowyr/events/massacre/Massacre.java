package fr.elowyr.events.massacre;

import com.massivecraft.factions.Faction;
import fr.elowyr.events.AbstractEvent;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.massacre.data.MassacreData;
import fr.elowyr.events.tasks.MassacreCooldownTask;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Massacre extends AbstractEvent {

    public MassacreManager massacreManager;

    public Massacre() {
        super("Massacre");
    }

    @Override
    public String[] getLines() {
        return new String[]{"§fEvent: §6§lMassacre", " §fCapturez les §e4 §fdrapeaux.", " §fGagnez §e5 §fpoints au classement."};
    }

    @Override
    public void setup() {
        massacreManager = new MassacreManager(this).setup();
    }

    @Override
    public void onStart() {
        massacreManager.setDelay(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));
        massacreManager.setTaskID(Bukkit.getScheduler().runTaskTimerAsynchronously(ElowyrEvents.getInstance(), new MassacreCooldownTask(), 0L, 20L));
    }

    @Override
    public void onStop() {
        Optional.ofNullable(massacreManager.getTaskID()).ifPresent(BukkitTask::cancel);
        List<MassacreData> datas = massacreManager.getWinners();
        if(!datas.isEmpty()) {
            MassacreData data = datas.get(0);
            if(data != null) {
                Faction faction = data.getFaction();
                if(faction != null) {
                    for(int i = 0; i < datas.size(); i++) {
                        MassacreData massacreData = datas.get(i);
                        int points = 0;
                        if(datas.indexOf(massacreData) == 0) {
                            points = 150;
                        } else if(datas.indexOf(massacreData) == 1) {
                            points = 125;
                        } else if (datas.indexOf(massacreData) == 2) {
                            points = 100;
                        }
                        for(String str : massacreManager.getRewards()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("%faction%", faction.getTag()).replace("%points%", String.valueOf(points)));
                        }
                    }
                }
            }
            massacreManager.getConfig().getString("MESSAGES.STOP.BROADCAST").replace("%faction%", data.getFaction().getTag());
        }
        massacreManager.getMassacreData().clear();
    }

    @Override
    public void onFinish() {
        Optional.ofNullable(massacreManager.getTaskID()).ifPresent(BukkitTask::cancel);
        List<MassacreData> datas = massacreManager.getWinners();
        List<String> winners = new LinkedList<>();
        if(!datas.isEmpty()) {
            MassacreData data = datas.get(0);
            if(data != null) {
                Faction faction = data.getFaction();
                if(faction != null) {
                    for(int i = 0; i < datas.size(); i++) {
                        MassacreData massacreData = datas.get(i);
                        int points = 0;
                        if(datas.indexOf(massacreData) == 0) {
                            points = 150;
                        } else if(datas.indexOf(massacreData) == 1) {
                            points = 125;
                        } else if (datas.indexOf(massacreData) == 2) {
                            points = 100;
                        }
                        for(String str : this.getRewards()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("%faction%", faction.getTag()).replace("%points%", String.valueOf(points)));
                        }
                        winners.add(faction.getTag());
                    }
                }
            }
            massacreManager.getConfig().getString("MESSAGES.STOP.BROADCAST").replace("%faction%", data.getFaction().getTag());
        }
        massacreManager.getMassacreData().clear();
        ElowyrEvents.getInstance().sendResult(this, winners);
    }

    @Override
    public boolean isFinish() {
        return massacreManager.hasExpired();
    }

    @Override
    public void onSecurize() {}

    @Override
    public void onUnsecurize() {}

    public void incrementPoints(Faction faction, int points) {
        MassacreData data = massacreManager.getDataByFaction(faction);
        if(data == null) {
            MassacreData newData = new MassacreData(faction, points);
            massacreManager.getMassacreData().add(newData);
        } else {
            data.incrementPoints(points);
        }
    }
}

