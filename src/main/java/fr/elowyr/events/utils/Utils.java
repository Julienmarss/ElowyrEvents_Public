package fr.elowyr.events.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.SplittableRandom;

public class Utils {

    private static long oneMinute;
    private static long oneHour;
    private static long oneDay;
    public static SimpleDateFormat frenchDateFormat;
    public static ThreadLocal<DecimalFormat> remainingSeconds;
    public static ThreadLocal<DecimalFormat> remainingSecondsTrailing;

    public static String color(final String value) {
        if (value == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public static List<String> colorAll(final List<String> value) {
        final ListIterator<String> iterator = value.listIterator();
        while (iterator.hasNext()) {
            iterator.set(color(iterator.next()));
        }
        return value;
    }

    public static boolean isInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int randomInt(int min, int max) {
        return new SplittableRandom().nextInt(min, max);
    }

    public static float randomFloat(float min, float max) {
        Random random = new SecureRandom();
        return min + random.nextFloat() * (max - min);
    }

    public static boolean isPlant(Block block) {
        Material type = block.getType();
        return type == Material.CROPS
                || type == Material.CARROT
                || type == Material.POTATO
                || type == Material.MELON_BLOCK
                || type == Material.PUMPKIN
                || type == Material.COCOA
                || type == Material.NETHER_WARTS
                || type == Material.STRING;
    }

    public static int getExperience(Block block) {
        Material type = block.getType();
        return type == Material.CARROT ? 1 :
                type == Material.POTATO ? 1 :
                        type == Material.WHEAT ? 1 :
                                type == Material.PUMPKIN ? 2 :
                                        type == Material.MELON_BLOCK ? 1 :
                                                type == Material.NETHER_WARTS ? 1 : 0;
    }

    public static int getRequiredMeta(Block block) {
        Material type = block.getType();
        //Dans le cas d'un bloc de melon ou une citrouille il n'y à pas de meta
        return type == Material.STRING || type == Material.COCOA ? 8 :
                type == Material.MELON || type == Material.PUMPKIN ? 0 :
                        type == Material.NETHER_WARTS ? 3 : 7;
    }

    public static String[] replaceAll(final String[] lines, final Object[] replacements) {
        for (int i = 0; i < lines.length; ++i) {
            lines[i] = replaceAll(lines[i], replacements);
        }
        return lines;
    }

    public static String replaceAll(String line, final Object[] replacements) {
        for (int i = 0; i < replacements.length; i += 2) {
            line = line.replace("%" + replacements[i] + "%", String.valueOf(replacements[i + 1]));
        }
        return line;
    }

    public static String itemTo64(ItemStack stack) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(stack);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    public static ItemStack itemFrom64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            try {

                return (ItemStack) dataInput.readObject();
            } finally {
                dataInput.close();
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}
