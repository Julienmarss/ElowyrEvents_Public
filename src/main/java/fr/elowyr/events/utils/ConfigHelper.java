package fr.elowyr.events.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigHelper {
    public static Map<String, Cuboid> from(ConfigurationSection section) {
        return section.getKeys(false).stream().collect(Collectors.toMap(Function.identity(), s -> readCuboid(section.getConfigurationSection(s))));
    }

    public static Set<Cuboid> fromD(ConfigurationSection section) {
        return section.getKeys(false).stream().map(s -> readCuboid(section.getConfigurationSection(s))).collect(Collectors.toSet());
    }

    public static Cuboid readCuboid(ConfigurationSection section) {
        World world = Bukkit.getWorld(section.getString("world-name"));
        Location firstCorner = readLocation(section.getConfigurationSection("first-corner"), world);
        Location secondCorner = readLocation(section.getConfigurationSection("second-corner"), world);
        return new Cuboid(firstCorner, secondCorner);
    }

    public static Location readLocation(ConfigurationSection section, World world) {
        return new Location(world,
                section.getDouble("x"),
                Optional.of(section.getDouble("y")).filter(d -> d > 0).orElse(65d),
                section.getDouble("z"));
    }

    public static Location readLocationD(ConfigurationSection section) {
        return new Location(Bukkit.getWorld(section.getString("world-name")),
                section.getDouble("x"),
                Optional.of(section.getDouble("y")).filter(d -> d > 0).orElse(65d),
                section.getDouble("z"));
    }
}
