package com.atlasplugins.atlasenchants.Listeners;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CreateCustomEnchant implements Listener {

    private Main main;

    public CreateCustomEnchant(Main main) {
        this.main = main;
    }

    public ItemStack CreateCustomEnchantmentItem(String enchantmentName, int enchantmentLevel, int enchantmentAmount, Player p) {
        ItemStack enchant = new ItemStack(Material.valueOf(main.getConfig().getString("EnchantItems.EnchantItem")));
        ItemMeta enchantMeta = enchant.getItemMeta();

        enchantMeta.setDisplayName(Main.color(main.getConfig().getString("Enchantments." + enchantmentName + ".Enchantment-Title"))
                .replace("{lvl}", String.valueOf(enchantmentLevel))
                .replace("{blacklistEnchant}", String.valueOf(main.getConfig().getStringList("Enchantments." + enchantmentName + ".Enchantment-Blacklist-Enchants")))
                .replace("{glowRange}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Radius-of-glowing-" + enchantmentLevel)))
                .replace("{time}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Time-underwater-" + enchantmentLevel)))
                .replace("{damage}", String.valueOf(main.getConfig().getDouble("Enchantments." + enchantmentName + ".Hunter-Damage-Amount-" + enchantmentLevel)))
                .replace("{speedLvl}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Rush-Speed-Amount-" + enchantmentLevel)))
                .replace("{speedTimer}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Rush-Speed-Timer-" + enchantmentLevel)))
                .replace("{block}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Propel-Height-Amount-" + enchantmentLevel)))
                .replace("{freezingTimer}", String.valueOf(main.getConfig().getDouble("Enchantments." + enchantmentName + ".FreezingShot-Freeze-Timer-" + enchantmentLevel)))
                .replace("{healthTimer}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".HealthBar-Timer-" + enchantmentLevel)))
                .replace("{extraHearts}", String.valueOf(main.getConfig().getDouble("Enchantments." + enchantmentName + ".Growth-Heart-Increase-" + enchantmentLevel)))
                .replace("{poisonTimer}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-Poison-Timer-" + enchantmentLevel)))
                .replace("{poisonLevel}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-Poison-Level-" + enchantmentLevel)))
                .replace("{stunTimer}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Stunning-Stun-Timer-" + enchantmentLevel)))
                .replace("{stunLevel}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Stunning-Stun-Levels-" + enchantmentLevel)))
                .replace("{iceTimer}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".IceAspect-Frozen-Timer-" + enchantmentLevel)))
                .replace("{extractorMultiplier}", String.valueOf(main.getConfig().getDouble("Enchantments." + enchantmentName + ".Extractor-EXP-Multiplier-" + enchantmentLevel)))
                .replace("{percent}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Leech-Healing-Amount-Percent-" + enchantmentLevel))));

        ArrayList<String> enchantmentLore = new ArrayList<>();
        List<String> loreList = main.getConfig().getStringList("Enchantments." + enchantmentName + ".Enchantment-Lore");
        for (String lore : loreList) {
            enchantmentLore.add(Main.color(lore)
                    .replace("{lvl}", String.valueOf(enchantmentLevel))
                    .replace("{blacklistEnchant}", String.valueOf(main.getConfig().getStringList("Enchantments." + enchantmentName + ".Enchantment-Blacklist-Enchants")))
                    .replace("{glowRange}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Radius-of-glowing-" + enchantmentLevel)))
                    .replace("{time}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Time-underwater-" + enchantmentLevel)))
                    .replace("{damage}", String.valueOf(main.getConfig().getDouble("Enchantments." + enchantmentName + ".Hunter-Damage-Amount-" + enchantmentLevel)))
                    .replace("{speedLvl}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Rush-Speed-Amount-" + enchantmentLevel)))
                    .replace("{speedTimer}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Rush-Speed-Timer-" + enchantmentLevel)))
                    .replace("{block}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Propel-Height-Amount-" + enchantmentLevel)))
                    .replace("{freezingTimer}", String.valueOf(main.getConfig().getDouble("Enchantments." + enchantmentName + ".FreezingShot-Freeze-Timer-" + enchantmentLevel)))
                    .replace("{healthTimer}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".HealthBar-Timer-" + enchantmentLevel)))
                    .replace("{extraHearts}", String.valueOf(main.getConfig().getDouble("Enchantments." + enchantmentName + ".Growth-Heart-Increase-" + enchantmentLevel)))
                    .replace("{poisonTimer}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-Poison-Timer-" + enchantmentLevel)))
                    .replace("{poisonLevel}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-Poison-Level-" + enchantmentLevel)))
                    .replace("{stunTimer}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Stunning-Stun-Timer-" + enchantmentLevel)))
                    .replace("{stunLevel}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Stunning-Stun-Levels-" + enchantmentLevel)))
                    .replace("{iceTimer}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".IceAspect-Frozen-Timer-" + enchantmentLevel)))
                    .replace("{extractorMultiplier}", String.valueOf(main.getConfig().getDouble("Enchantments." + enchantmentName + ".Extractor-EXP-Multiplier-" + enchantmentLevel)))
                    .replace("{percent}", String.valueOf(main.getConfig().getInt("Enchantments." + enchantmentName + ".Leech-Healing-Amount-Percent-" + enchantmentLevel))));
        }

        PersistentDataContainer pdc = enchantMeta.getPersistentDataContainer();
        pdc.set(Main.customEnchantKeys, PersistentDataType.STRING, enchantmentName + ":" + enchantmentLevel);

        enchantMeta.setLore(enchantmentLore);
        enchant.setItemMeta(enchantMeta);

        // Add items to player's inventory if player is not null
        if (p != null) {
            for (int i = 0; i < enchantmentAmount; i++) {
                p.getInventory().addItem(enchant);
            }
        }

        return enchant;
    }
}
