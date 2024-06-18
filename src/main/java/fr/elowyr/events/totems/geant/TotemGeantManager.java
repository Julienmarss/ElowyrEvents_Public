package fr.elowyr.events.totems.geant;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.koth.KOTHManager;
import fr.elowyr.events.totems.geant.listeners.TotemGeantListeners;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TotemGeantManager {

    private static final MessageFormat SCORE_FORMAT = new MessageFormat("{1} §e({2,number,#})");

    private final File file;
    private YamlConfiguration config;

    public final Map<Faction, Integer> scores = new ConcurrentHashMap<>();
    public final List<List<String>> rewards = new LinkedList<>();
    private final TotemGeant totem;
    public final int[] values = {8, 6, 4, 2, 1};
    public Map<String, Location> map = new HashMap<>();
    public Location location;
    private String factionId, playerName;
    public int time;

    public TotemGeantManager(TotemGeant totem) {
        this.totem = totem;
        this.file = new File(ElowyrEvents.getInstance().getDataFolder(), "totemgeant.yml");
        load();
    }

    private void load() {
        if (!this.file.exists()) {
            ElowyrEvents.getInstance().saveResource("totemgeant.yml", false);
        }
        try {
            this.config = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(Files.newInputStream(this.file.toPath()), StandardCharsets.UTF_8)));
            ElowyrEvents.getInstance().getLogger().info("[Events] TotemGeant loaded");
        }
        catch (Throwable ex) {
            ElowyrEvents.getInstance().getLogger().severe("[Events] Failed to load TotemGeant");
        }
    }

    @SuppressWarnings("unchecked")
    public TotemGeantManager setup() {
        for (final String key : this.config.getKeys(false)) {
            if (key.equals("rewards")) continue;
            World world = Bukkit.getWorld(config.getString(key + ".world-name"));
            double x = config.getDouble(key + ".x");
            double y = config.getDouble(key + ".y");
            double z = config.getDouble(key + ".z");
            this.map.put(key, new Location(world, x, y, z));
        }
        this.rewards.clear();
        this.rewards.addAll((List<List<String>>) config.getList("rewards"));
        registerPlaceHolder();
        totem.getListeners().add(new TotemGeantListeners(totem));
        return this;
    }

    public void registerPlaceHolder() {
        new PlaceholderExpansion() {

            @Override
            public String getIdentifier() {
                return "totemgeant";
            }

            @Override
            public String getAuthor() {
                return "AnZok";
            }

            @Override
            public String getVersion() {
                return "1.0";
            }

            @Override
            public String onPlaceholderRequest(Player player, String params) {
                Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                List<Map.Entry<Faction, Integer>> bestScores = orderedScores().limit(3).collect(Collectors.toList());
                if (params.equals("time_left")) {
                    return KOTHManager.FORMAT.format(getTime() * 1000);
                }
                if (params.equals("score")) {
                    return "" + getScore(faction);
                }

                for (int i = 0; i < 3; i++) {
                    if (params.equals("classement_" + i)) {
                        if (bestScores.size() >= i + 1) {
                            Map.Entry<Faction, Integer> score = bestScores.get(i);
                            return SCORE_FORMAT.format(new Object[]{i + 1, score.getKey().getTag(), score.getValue()});
                        } else {
                            return "§c✖";
                        }
                    }
                }

                return super.onPlaceholderRequest(player, params);
            }
        }.register();
    }

    public void spawnBlocks() {
        for (int i = 0; i < 5; i++) {
            Block block = location.clone().add(0, i, 0).getBlock();
            block.setType(Material.IRON_BLOCK);
            block.setMetadata("IRON_BLOCK", new FixedMetadataValue(ElowyrEvents.getInstance(), "IRON_BLOCK"));
        }
    }

    public void tick() {
        time--;
        ElowyrEvents.getInstance().getEvents().finishEvent(totem);
    }

    public int getBlocks() {
        int blocks = 0;
        for (int i = 0; i < 5; i++) {
            Block block = location.clone().add(0, i, 0).getBlock();
            if (block.getType() != Material.AIR && block.hasMetadata("IRON_BLOCK")) {
                blocks++;
            }
        }
        return blocks;
    }

    public Stream<Map.Entry<Faction, Integer>> orderedScores() {
        return this.scores
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));
    }


    public int getScore(Faction faction) {
        return this.scores.getOrDefault(faction, 0);
    }

    public boolean isFinish() {
        return time <= 0;
    }

    public int getTime() {
        return time;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Location getLocation() {
        return location;
    }

    public String getFactionId() {
        return factionId;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
