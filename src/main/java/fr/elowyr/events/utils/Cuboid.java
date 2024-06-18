package fr.elowyr.events.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cuboid {

    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;
    private final int zMin;
    private final int zMax;
    private final World world;

    public Cuboid(final Location point1, final Location point2) {
        this.xMin = Math.min(point1.getBlockX(), point2.getBlockX());
        this.xMax = Math.max(point1.getBlockX(), point2.getBlockX());
        this.yMin = Math.min(point1.getBlockY(), point2.getBlockY());
        this.yMax = Math.max(point1.getBlockY(), point2.getBlockY());
        this.zMin = Math.min(point1.getBlockZ(), point2.getBlockZ());
        this.zMax = Math.max(point1.getBlockZ(), point2.getBlockZ());
        this.world = point1.getWorld();
    }

    public List<Block> getWalls() {
        List<Block> blocks = new ArrayList<>();

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                blocks.add(world.getBlockAt(x, y, zMin));
                blocks.add(world.getBlockAt(x, y, zMax));
            }
        }
        for (int y = yMin; y <= yMax; y++) {
            for (int z = zMin; z <= zMax; z++) {
                blocks.add(world.getBlockAt(xMin, y, z));
                blocks.add(world.getBlockAt(xMax, y, z));
            }
        }
        return blocks;
    }

    public List<Block> getMaterialWalls(Material material) {
        return getWalls().stream().filter(block -> block.getType() == material).collect(Collectors.toList());
    }

    public boolean isIn(final Location location) {
        return location.getWorld().equals(this.world)
                && location.getBlockX() >= this.xMin && location.getBlockX() <= this.xMax
                && location.getBlockZ() >= this.zMin && location.getBlockZ() <= this.zMax
                && location.getBlockY() >= this.yMin && location.getBlockY() <= this.yMax;
    }

    public List<Player> getPlayers() {
        return world.getPlayers().stream().filter(this::isIn).filter(player -> !player.isDead()).collect(Collectors.toList());
    }

    public boolean isIn(final Player player) {
        return this.isIn(player.getLocation());
    }

    public World getWorld() {
        return world;
    }

    public int getxMin() {
        return xMin;
    }

    public int getzMax() {
        return zMax;
    }

    public int getyMax() {
        return yMax;
    }

    public int getyMin() {
        return yMin;
    }

    public int getxMax() {
        return xMax;
    }

    public int getzMin() {
        return zMin;
    }
}
