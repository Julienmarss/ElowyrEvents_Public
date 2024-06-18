package fr.elowyr.events.tasks;

import fr.elowyr.events.domination.Domination;
import fr.elowyr.events.geantkoth.GeantKoth;
import fr.elowyr.events.koth.KOTHManager;
import fr.elowyr.events.totems.classic.TotemManager;
import fr.elowyr.events.totems.geant.TotemGeantManager;
import fr.elowyr.events.utils.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AntiUseBugTask extends BukkitRunnable {

    private final List<Material> materials = Arrays.stream(Material.values()).filter(Material::isBlock).collect(Collectors.toList());
    private final List<Cuboid> cuboids = new ArrayList<>();

    public AntiUseBugTask(Object eventManager) {
        if (eventManager instanceof KOTHManager) {
            this.addCuboid(((KOTHManager) eventManager).getCurrent());
        } else if (eventManager instanceof GeantKoth) {
            this.addCuboid(((GeantKoth) eventManager).getCurrent());
        } else if (eventManager instanceof TotemGeantManager) {
            Location totemLocation = ((TotemGeantManager) eventManager).getLocation();
            this.addCuboid(new Cuboid(totemLocation, totemLocation));
        } else if (eventManager instanceof TotemManager) {
            Location totemLocation = ((TotemManager) eventManager).getLocation();
            this.addCuboid(new Cuboid(totemLocation, totemLocation));
        } else if (eventManager instanceof Domination) {
            ((Domination) eventManager).getCurrentZones().forEach(this::addCuboid);
        }
        /*
        else if (eventManager instanceof SanctuaireManager) {
            Arrays.stream(((SanctuaireManager) eventManager).getZones()).map(Zone::getCuboid).forEach(this::addCuboid);
        }
         */
    }

    public static Cuboid fixCuboid(Cuboid cuboid) {
        return new Cuboid(new Location(cuboid.getWorld(), cuboid.getxMin() - 10, cuboid.getyMin(), cuboid.getzMin() - 10), new Location(cuboid.getWorld(), cuboid.getxMax() + 10, cuboid.getyMax() + 100, cuboid.getzMax() + 10));
    }

    @Override
    public void run() {
        for (Cuboid cuboid : this.cuboids) {
            cuboid.getPlayers()
                    .stream()
                    .map(Player::getInventory)
                    .forEach(inventories -> Arrays.stream(inventories.getContents()).filter(Objects::nonNull).filter(itemStack -> materials.contains(itemStack.getType())).forEach(inventories::remove));
        }
    }

    public void addCuboid(Cuboid cuboid) {
        this.cuboids.add(fixCuboid(cuboid));
    }

    public List<Cuboid> getCuboids() {
        return cuboids;
    }
}
