package fr.elowyr.events.geantkoth;

import com.google.common.base.Preconditions;
import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import fr.elowyr.events.AbstractEvent;
import fr.elowyr.events.ElowyrEvents;
import fr.elowyr.events.domination.Domination;
import fr.elowyr.events.tasks.AntiUseBugTask;
import fr.elowyr.events.utils.ConfigHelper;
import fr.elowyr.events.utils.Cuboid;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeantKoth extends AbstractEvent {

    private final File file;
    private YamlConfiguration config;
    /**
     * The percentage to remove to a faction score when a player dies.
     * This percentage is used to calculate the death factor.
     */
    public static final short PERCENTAGE_PER_DEATH = 15;
    /**
     * The factor the faction score is multiplied by when a player dies
     */
    public static final float DEATH_FACTOR = 1 - PERCENTAGE_PER_DEATH / 100f;
    public static final MessageFormat FINAL_TOP_FORMAT = new MessageFormat("§f{0,choice,1#1ère|1<{0}ème} place: §e{1}");
    public static final MessageFormat SCORE_FORMAT = new MessageFormat("§f{0}: {1} §e({2,number,#}/{3,number,#})");
    private final Map<String, Cuboid> zones = new HashMap<>();
    private final Map<Faction, Integer> scores = new ConcurrentHashMap<>();
    //    private final Map<Faction, Integer> playersInZone = new ConcurrentHashMap<>();
    private final List<List<String>> rewards = new LinkedList<>();
    private final ElowyrEvents plugin = ElowyrEvents.getInstance();
    private int targetScore = 1500;
    private Cuboid current;
    private FPlayers players;
    private boolean finished = true;

    private BukkitTask timerTask;
    private AntiUseBugTask antiUseBugTask;
    private LCWaypoint waypoint;

    public GeantKoth() {
        super("geantkoth");
        this.file = new File(ElowyrEvents.getInstance().getDataFolder(), "geantkoth.yml");
        load();
    }

    /**
     * Get lines to be shown at the beginning of the event
     *
     * @return the lines
     */
    @Override
    public String[] getLines() {
        return new String[]{
                "§fEvent: §6§lKOTH Géant",
                " §fRestez dans la zone de capture jusqu'à §e" + this.targetScore + " points.",
                " §fChaque mort engendre une perte de §c" + PERCENTAGE_PER_DEATH + "% §fde tes points.",
                " §fLa 1ère place remporte §e15 §fpoints au classement."
        };
    }

    private void load() {
        if (!this.file.exists()) {
            ElowyrEvents.getInstance().saveResource("geantkoth.yml", false);
        }
        try {
            this.config = YamlConfiguration.loadConfiguration(new BufferedReader(new InputStreamReader(Files.newInputStream(this.file.toPath()), StandardCharsets.UTF_8)));
            ElowyrEvents.getInstance().getLogger().info("[Events] GeantKoth loaded");
        }
        catch (Throwable ex) {
            ElowyrEvents.getInstance().getLogger().severe("[Events] Failed to load GeantKoth");
        }
    }

    /**
     * Load configurations specific to the event
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setup() {
        this.zones.clear();
        for (final String key : this.config.getKeys(false)) {
            if (key.equals("rewards")) continue;
            this.targetScore = config.getInt(key + ".score");
            this.zones.putAll(ConfigHelper.from(config.getConfigurationSection(key + ".zones")));
        }
        this.rewards.clear();
        this.setRewards(config.getStringList("rewards"));
        // TODO check if it is really a list of lists
        registerPlaceHolder();
        this.getListeners().add(new GeantKothListener(this));
    }

    public void registerPlaceHolder() {
        new PlaceholderExpansion() {
            @Override
            public @NotNull String getIdentifier() {
                return "geantkoth";
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
            public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
                Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                List<Map.Entry<Faction, Integer>> bestScores = orderedScores().limit(3).collect(Collectors.toList());
                final String factionValue = Optional.ofNullable(faction).map(Faction::getTag).orElse("§c✖");
                if (params.equals("faction")) {
                    return factionValue;
                }
                if (params.equals("score")) {
                    return String.valueOf(scores.getOrDefault(faction, 0));
                }
                if (params.equals("max_points")) {
                    return String.valueOf(targetScore);
                }
                if (params.equals("penalty")) {
                    return "15";
                }
                for (int count = 0; count < 3; count++) {
                    if (params.equals("classement_" + count)) {
                        if (bestScores.size() <= count) {
                            return "§c✖";
                        }
                        Map.Entry<Faction, Integer> score = bestScores.get(count);
                        return Domination.SCORE_FORMAT.format(new Object[]{count + 1, score.getKey().getTag(), score.getValue(), targetScore});
                    }
                }
                return super.onPlaceholderRequest(player, params);
            }
        }.register();
    }

    /**
     * Start the event by resetting the values and times and setting the board to
     * the event's one
     */
    @Override
    public void onStart() {
        sendMessage("§6§lKOTH Géant §7§l• §fL'évènement a commencé.");
        this.reset();
        this.timerTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, 0, 20);
        waypoint = new LCWaypoint("Koth Geant",
                new Location(this.getCurrent().getWorld(), this.getCurrent().getxMax() - 1, this.getCurrent().getyMin(),
                        this.getCurrent().getzMax() - 1), new Color(255, 127, 0).getRGB(), true);
        for (Player player : Bukkit.getOnlinePlayers()) {
            LunarClientAPI.getInstance().sendWaypoint(player, waypoint);
        }
    }

    /**
     * Send a message and stop the event
     */
    @Override
    public void onStop() {
        this.sendMessage("§6§lKOTH Géant §7§l• §fL'évènement a été arrêté.");
        this.stop();
    }

    /**
     * Stop the event, show an informative message and give rewards
     */
    @Override
    public void onFinish() {
        List<Map.Entry<Faction, Integer>> scores = this.orderedScores().collect(Collectors.toList());
        LinkedList<String> winners = new LinkedList<>();
        if (scores.size() > 0) {
            this.sendMessage("§6§lKOTH Géant §7§l• §fL'évènement est terminé. Les gagnants sont:");
            for (int i = 0; i < scores.size() && i < 3; i++) {
                this.sendMessage(FINAL_TOP_FORMAT.format(new Object[]{i + 1, scores.get(i).getKey().getTag()}));
                winners.add(scores.get(i).getKey().getTag());
            }
        } else {
            this.sendMessage("§6§lKOTH Géant §7§l• §fAucune faction n'a remporté l'évènement.");
        }

        this.stop();
        for (int i = 0; i < scores.size() && i < this.rewards.size(); i++) {
            Faction faction = scores.get(i).getKey();
            String name = faction.getOnlinePlayers()
                    .stream()
                    .findAny()
                    .map(Player::getName)
                    .orElse(""); // if they won... They are online (skipped intensive checks)
            this.rewards.get(i)
                    .stream()
                    .map(s -> s.replaceAll("%faction%", faction.getTag()))
                    .map(s -> s.replaceAll("%player%", name))
                    .forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
        plugin.sendResult(this, winners);
    }

    /**
     * Get if the event is finished
     *
     * @return is finished
     */
    @Override
    public boolean isFinish() {
        return this.finished;
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
        this.antiUseBugTask = new AntiUseBugTask(this);
        this.antiUseBugTask.runTaskTimerAsynchronously(ElowyrEvents.getInstance(), 0L, 5L);
        this.antiUseBugTask.getCuboids().forEach(cuboid -> cuboid.getMaterialWalls(Material.BARRIER).forEach(block -> block.setType(Material.AIR)));
    }

    /**
     * Check if the given name is present in the zone list for this event
     *
     * @param name the name to check
     * @return weather the zone is present or not
     */
    public boolean isZonePresent(String name) {
        return this.zones.containsKey(name);
    }

    /**
     * Sets the zone for the next event
     *
     * @param name the name of the zone
     */
    public void setZone(String name) {
        this.current = this.zones.get(name);
    }

    /**
     * Get a list of zones names
     *
     * @return the names
     */
    public Set<String> getZones() {
        return this.zones.keySet();
    }

    public Cuboid getCurrentZone() {
        return this.current;
    }

    /**
     * Reset the event to default values for a restart
     */
    private void reset() {
        this.finished = false;
        this.players = FPlayers.getInstance();
        this.scores.clear();
    }

    /**
     * Event's tick function (actually call every 20 server ticks)
     * <p>
     * Gets all players present in the zone and increment the score of their factions.
     * The stream is parallel capable as every actions are threadsafe (event factions)
     */
    private void tick() {
//        this.playersInZone.clear();
        // Set the state to finished if any faction got the needed score
        this.finished = Bukkit.getOnlinePlayers()
                .parallelStream()
                .filter(this.current::isIn)
                .filter(player -> !player.isDead())
                .map(this.players::getByPlayer)  // tread safe
                .map(FPlayer::getFaction)
                // Count players in zone, slow down the algorithm but comply with specifications
                // TODO Benchmark the impact of the player counting algorithm
//                .peek(faction -> this.playersInZone.compute(faction, (f, i) -> (i == null ? 1 : i + 1)))
                .distinct()
                .filter(faction -> !faction.isWilderness())
                .map(faction -> this.scores.compute(faction, (f, i) -> i == null ? 1 : i + 1)) // increment scores
                .anyMatch(i -> i >= this.targetScore); // check endgame

        ElowyrEvents.getInstance().getEvents().finishEvent(this);
    }

    /**
     * Remove 15% score for the faction of the player who died
     *
     * @param faction the faction of the player who died
     * @return the new score
     */
    public int punish(Faction faction) {
        Integer integer = this.scores.computeIfPresent(faction, (f, i) -> (int) Math.floor(i * DEATH_FACTOR));
        return integer == null ? 0 : integer;
    }

    /**
     * Get the score of the given faction for the actual event
     *
     * @param faction the faction to get the score for
     * @return the actual score of the faction, or 0 if it did not participate;
     */
    public int getScore(Faction faction) {
        Preconditions.checkState(!this.finished, "Cannot get the score of a faction if the event is not running");
        return this.scores.getOrDefault(faction, 0);
    }

    /**
     * Effectively stops the event by canceling tasks
     */
    private void stop() {
        Optional.ofNullable(this.timerTask).ifPresent(BukkitTask::cancel);
        for (Player player : Bukkit.getOnlinePlayers()) {
            LunarClientAPI.getInstance().removeWaypoint(player, waypoint);
        }
        //this.onSecurize();
    }

    public Cuboid getCurrent() {
        return current;
    }

    /**
     * Get an ordered stream of the scores from the score map
     *
     * @return an ordered stream
     */
    private Stream<Map.Entry<Faction, Integer>> orderedScores() {
        return this.scores
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));
    }
}