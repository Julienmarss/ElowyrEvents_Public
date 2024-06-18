package fr.elowyr.events.ctf;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.ctf.flags.Flag;
import fr.elowyr.events.ctf.flags.FlagState;
import fr.elowyr.events.ctf.listeners.CTFListeners;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CTFManager {

    private final File file;
    private YamlConfiguration config;

    private final CTF ctf;
    private Location location;
    private Flag[] flags;
    private String factionId;
    private String playerName;
    private BukkitTask task;

    public CTFManager(CTF ctf) {
        this.ctf = ctf;
        this.file = new File(ElowyrEvents.getInstance().getDataFolder(), "ctf.yml");
        load();
    }

    public CTFManager setup() {
        World world = Bukkit.getWorld(config.getString("world-name"));
        location = new Location(world, config.getDouble("x"), config.getDouble("y"), config.getDouble("z"));
        setFlags(world);
        ctf.setRewards(config.getStringList("rewards"));
        registerPlaceHolder();
        ctf.getListeners().add(new CTFListeners(this));
        return this;
    }

    private void load() {
        if (!this.file.exists()) {
            ElowyrEvents.getInstance().saveResource("ctf.yml", false);
        }
        try {
            this.config = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(Files.newInputStream(this.file.toPath()), StandardCharsets.UTF_8)));
            ElowyrEvents.getInstance().getLogger().info("[Events] CaptureTheFlag loaded");
        }
        catch (Throwable ex) {
            ElowyrEvents.getInstance().getLogger().severe("[Events] Failed to load CaptureTheFlag");
        }
    }

    public void registerPlaceHolder() {
        new PlaceholderExpansion() {

            @Override
            public String getIdentifier() {
                return "ctf";
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
                String faction = getFactionId() != null ? Factions.getInstance().getFactionById(getFactionId()).getTag() : "§cNon";
                Flag flag = getFlag(player);
                if(params.equals("count")) {
                    return String.valueOf(countFlags());
                }
                if(params.equals("faction")) {
                    return faction;
                }
                if(params.equals("time_left")) {
                    return flag != null ? "" + new SimpleDateFormat("mm:ss").format(flag.getTime() * 1000) : "§cNon";
                }
                return super.onPlaceholderRequest(player, params);
            }
        }.register();
    }

    private int countFlags() {
        return Math.toIntExact(Arrays.stream(getFlags()).filter(flag -> flag.isFlagState(FlagState.PLACED)).count());
    }

    private void setFlags(World world) {
        ConfigurationSection section = config.getConfigurationSection("flags");
        List<Flag> cache = new ArrayList<>();
        for (String line : section.getKeys(false)) {
            Location location = new Location(world, section.getDouble(line + ".x"),
                    section.getDouble(line + ".y"), section.getDouble(line + ".z"));
            cache.add(new Flag(location));
        }
        flags = cache.toArray(new Flag[4]);
    }

    public Flag getFlag(Location location) {
        return Arrays.stream(flags).filter(
                flag -> flag.getLocation().getBlockX() == location.getBlockX() && flag.getLocation().getBlockY() == location.getBlockY()
                        && flag.getLocation().getBlockZ() == location.getBlockZ()).findFirst().orElse(null);
    }

    public Flag getFlag(Player player) {
        return Arrays.stream(flags).filter(flag -> flag.getPlayerName() != null && flag.getPlayerName().equals(player.getName())).findFirst().orElse(null);
    }

    public void restoreHelmet(Flag flag) {
        if (flag.getPlayerName() == null) {
            return;
        }
        Player player = Bukkit.getPlayer(flag.getPlayerName());
        ItemStack itemStack = flag.getHelmet() != null ? flag.getHelmet() : new ItemStack(Material.AIR);
        player.getInventory().setHelmet(itemStack);
        flag.setPlayerName(null);
    }

    public void captureFlag(Player player, Flag flag) {
        Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
        if (!faction.isNormal()) {
            return;
        }
        flag.setFlagState(FlagState.CAPTURED);
        flag.setPlayerName(player.getName());
        flag.setFactionId(faction.getId());
        Location location = flag.getLocation();
        location.getBlock().setType(Material.AIR);
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && helmet.getType() != Material.AIR) {
            flag.setHelmet(helmet.clone());
        }
        player.getInventory().setHelmet(new ItemStack(Material.BANNER));
        ctf.sendMessage("§6§lCTF §7§l• §e" + player.getName() + " §fde la faction §b" + faction.getTag() + " §fa capturé un drapeau.");
    }

    public void dropFlag(Player player, Flag flag) {
        if (wrongLocation(player.getLocation()) || !flag.isFlagState(FlagState.CAPTURED)) {
            return;
        }
        flag.setFlagState(FlagState.PLACED);
        Faction faction = Factions.getInstance().getFactionById(flag.getFactionId());
        ctf.sendMessage("§6§lCTF §7§l• §e" + player.getName() + " §fde la faction §b" + faction.getTag() + " §fa rapporté un drapeau.");
        restoreHelmet(flag);
        cancelFlags(faction, flag);
        factionId = faction.getId();
        playerName = player.getName();
        ElowyrEvents.getInstance().getEvents().finishEvent(ctf);
    }

    public void cancelFlags(Faction faction, Flag flag) {
        if (factionId == null || factionId.equals(faction.getId())) {
            return;
        }
        Arrays.stream(flags).filter(current -> current.isFlagState(FlagState.PLACED)
                && !current.equals(flag)).forEach(this::spawnBanner);
        ctf.sendMessage("§6§lCTF §7§l• §fLa faction §b" + faction.getTag() + " §fa annulé la capture des drapeaux.");
    }

    private boolean wrongLocation(Location to) {
        return !to.getWorld().equals(location.getWorld()) || to.distanceSquared(location) > 2;
    }

    public void spawnBanner(Flag flag) {
        flag.setFlagState(FlagState.AVAILABLE);
        flag.setTime(300);
        flag.setFactionId(null);
        flag.setHelmet(null);
        flag.setPlayerName(null);
        flag.getLocation().getBlock().setType(Material.STANDING_BANNER);
    }

    public void sendRespawnAlert(Flag flag) {
        Location location = flag.getLocation();
        ctf.sendMessage("§6§lCTF §7§l• §fUn drapeau a réapparu ! §c(" + location.getBlockX()
                + ", " + location.getBlockZ() + ")");
    }

    public void startTask() {
        task = Bukkit.getScheduler().runTaskTimer(ElowyrEvents.getInstance(), () -> {
            for (Flag flag : flags) {
                if (!flag.isFlagState(FlagState.CAPTURED)) {
                    continue;
                }
                flag.setTime(flag.getTime() - 1);
                if (flag.getTime() <= 0) {
                    restoreHelmet(flag);
                    spawnBanner(flag);
                }
            }
        }, 0, 20);
    }

    public Flag[] getFlags() {
        return flags;
    }

    public String getFactionId() {
        return factionId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setFactionId(String factionId) {
        this.factionId = factionId;
    }

    public BukkitTask getTask() {
        return task;
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}
