package fr.elowyr.events.massacre;

import com.google.common.collect.Lists;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.massacre.data.MassacreData;
import fr.elowyr.events.massacre.listeners.MassacreListener;
import fr.elowyr.events.utils.DurationFormatter;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter @Setter
public class MassacreManager {
    private static MassacreManager instance;

    private final Massacre massacre;
    private BukkitTask taskID;
    private long delay;
    public List<MassacreData> massacreData;
    private final File file;
    private YamlConfiguration config;

    private Map<EntityType, Integer> points;
    private HashMap<UUID, Integer> players;
    private List<String> rewards;

    public MassacreManager(Massacre massacre) {
        instance = this;
        this.massacre = massacre;
        this.file = new File(ElowyrEvents.getInstance().getDataFolder(), "massacre.yml");
        load();
        this.massacreData = Lists.newArrayList();
    }

    private void load() {
        if (!this.file.exists()) {
            ElowyrEvents.getInstance().saveResource("massacre.yml", false);
        }
        try {
            this.config = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(Files.newInputStream(this.file.toPath()), StandardCharsets.UTF_8)));
            for (String mob : config.getStringList("mobs")) {
                String[] parts = mob.split(":");
                this.points.put(EntityType.valueOf(parts[0]), Integer.parseInt(parts[1]));
            }
            this.setRewards(config.getStringList("rewards"));
            ElowyrEvents.getInstance().getLogger().info("Massacre loaded");
        } catch (Throwable ex) {
            ElowyrEvents.getInstance().getLogger().severe("Failed to load Massacre");
        }
    }

    public MassacreManager setup() {
        registerPlaceHolder();
        massacre.getListeners().add(new MassacreListener(massacre));
        return this;
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
                if (massacre.isStarted()) {
                    for (int i = 0; i < getWinners().size(); i++) {
                        if (identifier.equalsIgnoreCase(i + "_faction")) {
                            if (getWinners().size() < i) {
                                return "§c✖";
                            }
                            return getWinners().get(i).getFaction().getTag();
                        }
                        if (identifier.equalsIgnoreCase(i + "_points")) {
                            if (getWinners().size() < i) {
                                return "0";
                            }
                            return String.valueOf(getWinners().get(i).getPoints());
                        }
                    }
                    if (identifier.equalsIgnoreCase("faction")) {
                        return (getDataByFaction(faction) == null ? "§c✖" : getDataByFaction(faction).getFaction().getTag());
                    }
                    if (identifier.equalsIgnoreCase("position")) {
                        return "" + getClassementData(faction);
                    }
                    if (identifier.equalsIgnoreCase("your_points")) {
                        return (getDataByFaction(faction) == null ? "§c✖" : "" + getDataByFaction(faction).getPoints());
                    }
                    if (identifier.equalsIgnoreCase("time_left")) {
                        return DurationFormatter.getRemaining(getRemaining(), false);
                    }
                }
                return super.onPlaceholderRequest(player, identifier);
            }
        }.register();
    }

    public void incrementPoints(Faction faction, int points) {
        MassacreData data = this.getDataByFaction(faction);
        if(data == null) {
            MassacreData newData = new MassacreData(faction, points);
            this.massacreData.add(newData);
        } else {
            data.incrementPoints(points);
        }
    }

    public List<MassacreData> getWinners() {
        List<MassacreData> winnersData = this.massacreData;
        return winnersData.stream().sorted(Comparator.comparingInt(MassacreData::getPoints).reversed()).limit(3).collect(Collectors.toList());
    }

    public MassacreData getDataByFaction(Faction faction) {
        return this.massacreData.stream().filter(data -> data.getFaction().getTag().equalsIgnoreCase(faction.getTag())).findFirst().orElse(null);
    }

    public int getClassementData(Faction faction) {
        return this.massacreData.indexOf(this.getDataByFaction(faction)) + 1;
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

    public static MassacreManager getInstance() {
        return instance;
    }
}
