package fr.elowyr.events.farm;

import com.google.common.collect.Lists;
import com.massivecraft.factions.Faction;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.farm.data.FarmData;
import fr.elowyr.events.farm.listeners.FarmListener;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter @Setter
public class FarmManager {
    private static FarmManager instance;

    private final Farm farm;
    public List<FarmData> farmData;
    private BukkitTask taskID;
    private long delay;
    private final File file;
    private YamlConfiguration config;
    private List<String> rewards;

    public FarmManager(Farm farm) {
        instance = this;
        this.farm = farm;
        this.file = new File(ElowyrEvents.getInstance().getDataFolder(), "farm.yml");
        this.farmData = Lists.newArrayList();
    }

    public FarmManager setup() {
        if (!this.file.exists()) {
            ElowyrEvents.getInstance().saveResource("farm.yml", false);
        }
        try {
            this.config = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(Files.newInputStream(this.file.toPath()), StandardCharsets.UTF_8)));
            this.setRewards(config.getStringList("rewards"));
            ElowyrEvents.getInstance().getLogger().info("Farm loaded");
        }
        catch (Throwable ex) {
            ElowyrEvents.getInstance().getLogger().severe("Failed to load Farm");
        }
        farm.getListeners().add(new FarmListener(farm));
        return this;
    }

    public void incrementPoints(Faction faction, int points) {
        FarmData data = this.getDataByFaction(faction);
        if(data == null) {
            FarmData newData = new FarmData(faction, points);
            this.farmData.add(newData);
        } else {
            data.incrementPoints(points);
        }
    }

    public List<FarmData> getWinners() {
        List<FarmData> winnersData = this.farmData;
        return winnersData.stream().sorted(Comparator.comparingInt(FarmData::getPoints).reversed()).limit(3).collect(Collectors.toList());
    }

    public FarmData getDataByFaction(Faction faction) {
        return this.farmData.stream().filter(data -> data.getFaction().getTag().equalsIgnoreCase(faction.getTag())).findFirst().orElse(null);
    }

    public int getClassementData(Faction faction) {
        return this.farmData.indexOf(this.getDataByFaction(faction)) + 1;
    }

    public long getRemaining() {
        return this.delay - System.currentTimeMillis();
    }

    public void resetDelay() {
        this.delay = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);
        //MAJ remettre le temps
    }

    public boolean hasExpired() {
        return this.getRemaining() <= 0L;
    }

    public static FarmManager getInstance() {
        return instance;
    }
}
