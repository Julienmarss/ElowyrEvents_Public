package fr.elowyr.events.utils;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bukkit/Spigot/Bungee Java Plugin Maker.
 * EnervateD. All rights reserved.
 */
public class BarUtil {

    private static Map<String, EntityEnderDragon> dragons = new ConcurrentHashMap<>();

    public static void setBar(Player p, String text, float healthPercent) {
        Location loc = p.getLocation();
        WorldServer world = ((CraftWorld) p.getLocation().getWorld()).getHandle();

        EntityEnderDragon dragon = new EntityEnderDragon(world);
        dragon.setLocation(loc.getX(), loc.getY() - 100, loc.getZ(), 0, 0);

        PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(dragon);

        DataWatcher watcher = new DataWatcher(null);
        watcher.a(0, (byte) 0x20);
        watcher.a(6, (healthPercent * 200) / 100);
        watcher.a(10, text);
        watcher.a(2, text);
        watcher.a(11, (byte) 1);
        watcher.a(3, (byte) 1);

        try{
            Field t = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("l");
            t.setAccessible(true);
            t.set(packet, watcher);
        } catch(Exception ex){
            ex.printStackTrace();
        }

        dragons.put(p.getName(), dragon);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public static void removeBar(Player p) {
        if(dragons.containsKey(p.getName())) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(dragons.get(p.getName()).getId());
            dragons.remove(p.getName());
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void teleportBar(Player p) {
        if(dragons.containsKey(p.getName())) {
            Location loc = p.getLocation();
            PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(dragons.get(p.getName()).getId(),
                    (int) loc.getX() * 32, (int) (loc.getY() - 100) * 32, (int) loc.getZ() * 32,
                    (byte) ((int) loc.getYaw() * 256 / 360), (byte) ((int) loc.getPitch() * 256 / 360), false);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void updateText(Player p, String text) {
        updateBar(p, text, -1);
    }

    public static void updateHealth(Player p, float healthPercent) {
        updateBar(p, null, healthPercent);
    }

    public static void updateBar(Player p, String text, float healthPercent) {
        if(dragons.containsKey(p.getName())) {
            DataWatcher watcher = new DataWatcher(null);
            watcher.a(0, (byte) 0x20);
            if (healthPercent != -1) watcher.a(6, (healthPercent * 200) / 100);
            if (text != null) {
                watcher.a(10, text);
                watcher.a(2, text);
            }
            watcher.a(11, (byte) 1);
            watcher.a(3, (byte) 1);

            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(dragons.get(p.getName()).getId(), watcher, true);
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static Set<String> getPlayers() {
        Set<String> set = new HashSet<>();

        for(Map.Entry<String, EntityEnderDragon> entry : dragons.entrySet()) {
            set.add(entry.getKey());
        }

        return set;
    }

}