package fr.elowyr.events.totems.classic;

import com.massivecraft.factions.Factions;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.totems.classic.listeners.TotemListeners;
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
import java.util.HashMap;
import java.util.Map;

public class TotemManager {

    private final File file;
    private YamlConfiguration config;

    private final Totem totem;
    public Map<String, Location> map = new HashMap<>();
    public Map<Integer, String> playersCache = new HashMap<>();
    public Location location;
    private String playerName, factionId;
    private boolean[] blocksPresent;

    public TotemManager(Totem totem) {
        this.totem = totem;
        this.file = new File(ElowyrEvents.getInstance().getDataFolder(), "totem.yml");
        load();
    }

    private void load() {
        if (!this.file.exists()) {
            ElowyrEvents.getInstance().saveResource("totem.yml", false);
        }
        try {
            this.config = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(Files.newInputStream(this.file.toPath()), StandardCharsets.UTF_8)));
            ElowyrEvents.getInstance().getLogger().info("[Events] Totem loaded");
        }
        catch (Throwable ex) {
            ElowyrEvents.getInstance().getLogger().severe("[Events] Failed to load Totem");
        }
    }

    public TotemManager setup() {

        for (final String key : this.config.getKeys(false)) {
            if (key.equals("rewards")) continue;
            World world = Bukkit.getWorld(config.getString(key + ".world-name"));
            double x = config.getDouble(key + ".x");
            double y = config.getDouble(key + ".y");
            double z = config.getDouble(key + ".z");
            this.map.put(key, new Location(world, x, y, z));
        }
        totem.setRewards(config.getStringList("rewards"));
        registerPlaceHolder();
        totem.getListeners().add(new TotemListeners(totem));
        return this;
    }

    public void registerPlaceHolder() {
        new PlaceholderExpansion() {

            @Override
            public String getIdentifier() {
                return "totem";
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
                final String breaker = playerName == null ? "§c✖" : playerName;
                final String faction = factionId == null ? "§c✖" : Factions.getInstance().getFactionById(factionId).getTag();
                if(params.equals("breaker")) {
                    return breaker;
                }
                if(params.equals("faction")) {
                    return faction;
                }
                if(params.equals("block_remaining")) {
                    return String.valueOf(getBlocks());
                }

                for(int i = getBlocksPresent().length - 1; i >= 0; i--) {
                    if(params.equals("block_" + i)) {
                        return getBlocksPresent()[i] ? "⬛" : "⬜" + "   §l❘   §e" + playersCache.get(i);
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
        this.blocksPresent = new boolean[]{true, true, true, true, true};
        this.playersCache.clear();
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

    public boolean isFinish() {
        return getBlocks() == 0;
    }

    public Location getLocation() {
        return location;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean[] getBlocksPresent() {
        return blocksPresent;
    }

    public String getFactionId() {
        return factionId;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }

}
