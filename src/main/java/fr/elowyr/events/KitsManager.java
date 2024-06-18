package fr.elowyr.events;

import fr.elowyr.events.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class KitsManager {

    public ItemStack[] getBattleRoyalArmor(String prefix) {
        return new ItemStack[]{
                new ItemBuilder(Material.DIAMOND_BOOTS).setName(" §6§lBottes " + prefix).addEnchant(Enchantment.DURABILITY, 3).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).create(),
                new ItemBuilder(Material.DIAMOND_LEGGINGS).setName(" §6§lPantalon " + prefix).addEnchant(Enchantment.DURABILITY, 3).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).create(),
                new ItemBuilder(Material.DIAMOND_CHESTPLATE).setName(" §6§lPlastron " + prefix).addEnchant(Enchantment.DURABILITY, 3).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).create(),
                new ItemBuilder(Material.DIAMOND_HELMET).setName(" §6§lCasque " + prefix).addEnchant(Enchantment.DURABILITY, 3).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).create(),
        };
    }

    public ItemStack[] getBattleRoyalContents(String prefix) {
        final List<ItemStack> kitToList = new ArrayList<>();
        kitToList.add(new ItemBuilder(Material.DIAMOND_SWORD).setName(" §6§lEpee " + prefix).addEnchant(Enchantment.DURABILITY, 5).addEnchant(Enchantment.DAMAGE_ALL, 5).addEnchant(Enchantment.FIRE_ASPECT, 2).create());
        kitToList.add(new ItemStack(Material.GOLDEN_APPLE, 16));
        kitToList.add(new ItemBuilder(Material.BOW).setName(" §6§lArc " + prefix).addEnchant(Enchantment.ARROW_KNOCKBACK, 2).addEnchant(Enchantment.ARROW_INFINITE, 1).create());
        kitToList.add(new ItemStack(Material.ENDER_PEARL, 8));
        kitToList.add(new ItemStack(Material.COOKED_BEEF, 64));
        ItemStack potion = new Potion(PotionType.INSTANT_HEAL, 2, true).toItemStack(1);
        for (int i = 0; i < 30; i++) {
            kitToList.add(potion);
        }
        kitToList.add(new ItemStack(Material.ARROW));
        return kitToList.toArray(new ItemStack[0]);
    }

    public PotionEffect[] getTeamFightEffects() {
        return new PotionEffect[]{new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0), new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0)};
    }

    public ItemStack[] getTeamFightArmor() {
        return new ItemStack[]{
                new ItemBuilder(Material.DIAMOND_BOOTS).setName(" §6§lBottes TeamFight").addEnchant(Enchantment.DURABILITY, 4).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).create(),
                new ItemBuilder(Material.DIAMOND_LEGGINGS).setName(" §6§lPantalon TeamFight").addEnchant(Enchantment.DURABILITY, 4).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).create(),
                new ItemBuilder(Material.DIAMOND_CHESTPLATE).setName(" §6§lPlastron TeamFight").addEnchant(Enchantment.DURABILITY, 4).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).create(),
                new ItemBuilder(Material.DIAMOND_HELMET).setName(" §6§lCasque TeamFight").addEnchant(Enchantment.DURABILITY, 4).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4).create(),
        };
    }

    public ItemStack[] getTeamFightContents() {
        final List<ItemStack> kitToList = new ArrayList<>();
        kitToList.add(new ItemBuilder(Material.DIAMOND_SWORD).setName(" §6§lEpee TeamFight").addEnchant(Enchantment.DURABILITY, 5).addEnchant(Enchantment.DAMAGE_ALL, 5).addEnchant(Enchantment.FIRE_ASPECT, 2).create());
        kitToList.add(new ItemStack(Material.GOLDEN_APPLE, 32));
        kitToList.add(new ItemBuilder(Material.BOW).setName(" §6§lArc TeamFight").addEnchant(Enchantment.ARROW_KNOCKBACK, 2).addEnchant(Enchantment.ARROW_INFINITE, 1).addEnchant(Enchantment.DURABILITY, 5).create());
        kitToList.add(new ItemStack(Material.ENDER_PEARL, 16));
        ItemStack potion = new Potion(PotionType.INSTANT_HEAL, 2, true).toItemStack(1);
        for (int i = 0; i < 31; i++) {
            kitToList.add(potion);
        }
        kitToList.add(new ItemStack(Material.ARROW));
        return kitToList.toArray(new ItemStack[0]);
    }

    public PotionEffect[] getBattleRoyalEffects() {
        return new PotionEffect[]{new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1), new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0), new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0)};
    }
}
