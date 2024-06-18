package fr.elowyr.events.farm;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.elowyr.events.AbstractEvent;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.farm.data.FarmData;
import fr.elowyr.events.tasks.FarmCooldownTask;
import fr.elowyr.events.utils.DurationFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Farm extends AbstractEvent {

    public FarmManager farmManager;

    public Farm() {
        super("Farm");
    }

    @Override
    public String[] getLines() {
        return new String[]{"§fEvent: §6§lTournoi Agricole", " §fCapturez les §e4 §fdrapeaux.", " §fGagnez §e5 §fpoints au classement."};
    }

    @Override
    public void setup() {
        farmManager = new FarmManager(this).setup();
        registerPlaceHolder();
    }

    public void registerPlaceHolder() {
        new PlaceholderExpansion() {

            @Override
            public String getIdentifier() {
                return "farm";
            }

            @Override
            public String getAuthor() {
                return "AnZok";
            }

            @Override
            public String getVersion() {
                return "1.0.0";
            }

            @Override
            public String onPlaceholderRequest(Player player, String identifier) {
                Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                if (farmManager.getFarm().isStarted()) {
                    for (int i = 0; i < farmManager.getWinners().size(); i++) {
                        if (identifier.equalsIgnoreCase("time_left")) {
                            return DurationFormatter.getRemaining(farmManager.getRemaining(), false);
                        }
                        if (identifier.equalsIgnoreCase("your_faction")) {
                            return (farmManager.getDataByFaction(faction) == null ? "§c✖" : farmManager.getDataByFaction(faction).getFaction().getTag());
                        }
                        if (identifier.equalsIgnoreCase("your_points")) {
                            return (farmManager.getDataByFaction(faction) == null ? "§c✖" : "" + farmManager.getDataByFaction(faction).getPoints());
                        }
                    }

                    for (int count = 0; count < 3; count++) {
                        if(identifier.equals("classement_" + count)) {
                            if(farmManager.getWinners().size() <= count) {
                                return "§c✖ &7- §c✖";
                            }
                            return farmManager.getWinners().get(count).getFaction().getTag() + " &7- " + farmManager.getWinners().get(count).getPoints();
                        }
                    }
                }
                return super.onPlaceholderRequest(player, identifier);
            }
        }.register();
    }

    @Override
    public void onStart() {
        farmManager.setDelay(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)); //MAJ remettre le temps
        farmManager.setTaskID(Bukkit.getScheduler().runTaskTimerAsynchronously(ElowyrEvents.getInstance(), new FarmCooldownTask(), 0L, 20L));
        for (Faction faction : Factions.getInstance().getAllFactions()) {
            if (faction.isWarZone() || faction.isSafeZone() || faction.isWilderness()) return;
            farmManager.getFarmData().add(new FarmData(faction, 0));
        }
    }

    @Override
    public void onStop() {
        Optional.ofNullable(farmManager.getTaskID()).ifPresent(BukkitTask::cancel);
        List<FarmData> datas = farmManager.getWinners();
        if(!datas.isEmpty()) {
            FarmData data = datas.get(0);
            if(data != null) {
                Faction faction = data.getFaction();
                if(faction != null) {
                    for(int i = 0; i < datas.size(); i++) {
                        FarmData farmData = datas.get(i);
                        int points = 0;
                        if(datas.indexOf(farmData) == 0) {
                            points = 150;
                        } else if(datas.indexOf(farmData) == 1) {
                            points = 125;
                        } else if (datas.indexOf(farmData) == 2) {
                            points = 100;
                        }
                        for(String str : farmManager.getRewards()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("%faction%", faction.getTag()).replace("%points%", String.valueOf(points)));
                        }
                    }
                }
            }
            farmManager.getConfig().getString("MESSAGES.STOP.BROADCAST").replace("%faction%", data.getFaction().getTag());
        }
        farmManager.getFarmData().clear();
    }

    @Override
    public void onFinish() {
        Optional.ofNullable(farmManager.getTaskID()).ifPresent(BukkitTask::cancel);
        List<FarmData> datas = farmManager.getWinners();
        List<String> winners = new LinkedList<>();
        if(!datas.isEmpty()) {
            FarmData data = datas.get(0);
            if(data != null) {
                Faction faction = data.getFaction();
                if(faction != null) {
                    for(int i = 0; i < datas.size(); i++) {
                        FarmData farmData = datas.get(i);
                        int points = 0;
                        if(datas.indexOf(farmData) == 0) {
                            points = 150;
                        } else if(datas.indexOf(farmData) == 1) {
                            points = 125;
                        } else if (datas.indexOf(farmData) == 2) {
                            points = 100;
                        }
                        for(String str : this.getRewards()) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), str.replace("%faction%", faction.getTag()).replace("%points%", String.valueOf(points)));
                        }
                        winners.add(faction.getTag());
                    }
                }
            }
            farmManager.getConfig().getString("MESSAGES.STOP.BROADCAST").replace("%faction%", data.getFaction().getTag());
        }
        farmManager.getFarmData().clear();
        ElowyrEvents.getInstance().sendResult(this, winners);
    }

    @Override
    public boolean isFinish() {
        return farmManager.hasExpired();
    }

    @Override
    public void onSecurize() {}

    @Override
    public void onUnsecurize() {}

    public void incrementPoints(Faction faction, int points) {
        FarmData data = farmManager.getDataByFaction(faction);
        if(data == null) {
            FarmData newData = new FarmData(faction, points);
            farmManager.getFarmData().add(newData);
        } else {
            data.incrementPoints(points);
        }
    }
}

