package fr.elowyr.events.koth;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.utils.Cuboid;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public class KOTHManager {

    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("mm:ss");

    private final File file;
    private YamlConfiguration config;

    private static final int MAX_TIME = 240;
    private static final int CHANGE_TIME = 450;
    private final KOTH koth;
    public Map<String, Cuboid> cuboid = new HashMap<>();
    public Cuboid current;
    private String playerName, factionId;
    private int time, totalTime, reducer;


    public KOTHManager(KOTH koth) {
        this.koth = koth;
        this.file = new File(ElowyrEvents.getInstance().getDataFolder(), "koth.yml");
        load();
    }

    private void load() {
        if (!this.file.exists()) {
            ElowyrEvents.getInstance().saveResource("koth.yml", false);
        }
        try {
            this.config = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(Files.newInputStream(this.file.toPath()), StandardCharsets.UTF_8)));
            ElowyrEvents.getInstance().getLogger().info("[Events] Koth loaded");
        }
        catch (Throwable ex) {
            ElowyrEvents.getInstance().getLogger().severe("[Events] Failed to load koth");
        }
    }

    public KOTHManager setup() {
        for (final String key : this.config.getKeys(false)) {
            if (key.equals("rewards")) continue;
            World world = Bukkit.getWorld(config.getString(key + ".world-name"));
            ConfigurationSection locations = config.getConfigurationSection(key + ".locations");
            Location firstCorner = new Location(world, locations.getDouble("first-corner.x"), locations.getDouble("first-corner.y"), locations.getDouble("first-corner.z"));
            Location secondCorner = new Location(world, locations.getDouble("second-corner.x"), locations.getDouble("second-corner.y"), locations.getDouble("second-corner.z"));
            cuboid.put(key, new Cuboid(firstCorner, secondCorner));
        }
        koth.setRewards(config.getStringList("rewards"));
        koth.getListeners().add(new KOTHListeners(this));
        registerPlaceHolder();
        totalTime = 0;
        reducer = 0;
        return this;
    }

    public void registerPlaceHolder() {
        new PlaceholderExpansion() {
            @Override
            public @NotNull String getIdentifier() {
                return "koth";
            }

            @Override
            public @NotNull String getAuthor() {
                return "AnZok";
            }

            @Override
            public @NotNull String getVersion() {
                return "1.0";
            }

            @Override
            public String onPlaceholderRequest(Player player, String params) {
                switch (params.toLowerCase()) {
                    case "capture":
                        return playerName == null ? "§c✖" : playerName;
                    case "faction":
                        return factionId == null ? "§c✖" : Factions.getInstance().getFactionById(factionId).getTag();
                    case "time_left":
                        return "" + FORMAT.format(getTime() * 1000);
                    case "time_total":
                        return "" + FORMAT.format(getTotalTime() * 1000);
                }
                return super.onPlaceholderRequest(player, params);
            }
        }.register();
    }

    public void reset() {
        factionId = null;
        playerName = null;
        time = MAX_TIME - reducer;
    }

    public void makeCaptureArea() {
        if (playerName != null) {
            this.setOut();
        } else {
            List<Player> list = new ArrayList<>(Bukkit.getOnlinePlayers());
            Collections.shuffle(list);
            for (Player player : list) {
                if (this.captureArea(player)) {
                    break;
                }
            }
        }
    }

    public void makeTime() {
        if (playerName != null) {
            this.time--;
        }
        this.totalTime++;
        if ((totalTime % CHANGE_TIME == 0) && (reducer + 90 < MAX_TIME)) {
            reducer += 30;
            koth.sendMessage("§6§lKOTH §7§l• §b" + FORMAT.format(totalTime * 1000) + "m §fse sont écoulées, réduction de la durée de cap à §b" + FORMAT.format((MAX_TIME - reducer) * 1000) + "m §f!");
            if (playerName == null) {
                this.time = MAX_TIME - reducer;
            }
        }
        ElowyrEvents.getInstance().getEvents().finishEvent(koth);
    }

    private void setOut() {
        if (playerName == null) {
            return;
        }
        Player player = Bukkit.getPlayer(playerName);
        if (player != null && player.isOnline()) {
            Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
            if (!player.isDead() && this.current.isIn(player) && faction.getId().equals(factionId)) {
                return;
            }
        }
        this.reset();
    }

    private boolean captureArea(Player player) {
        if (playerName != null || !player.isOnline() || player.isDead() || !this.current.isIn(player)) {
            return false;
        }

        Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
        if (!faction.isNormal()) {
            return false;
        }
        this.playerName = player.getName();
        this.factionId = faction.getId();
        return true;
    }

    public Cuboid getCurrent() {
        return current;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getFactionId() {
        return factionId;
    }

    public int getTime() {
        return time;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
