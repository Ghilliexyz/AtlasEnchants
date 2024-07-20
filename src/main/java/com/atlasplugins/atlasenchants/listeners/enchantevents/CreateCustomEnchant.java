package com.atlasplugins.atlasenchants.listeners.enchantevents;

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
        ItemStack enchant = new ItemStack(Material.valueOf(main.getSettingsConfig().getString("EnchantItems.EnchantItem")));
        ItemMeta enchantMeta = enchant.getItemMeta();

        String displayName = main.getEnchantmentsConfig().getString("Enchantments." + enchantmentName + ".Enchantment-Title");
        String withPAPISet = main.setPlaceholders(p, displayName);
        enchantMeta.setDisplayName(Main.color(withPAPISet)
                .replace("{lvl}", String.valueOf(enchantmentLevel))
                .replace("{blacklistEnchant}", String.valueOf(main.getEnchantmentsConfig().getStringList("Enchantments." + enchantmentName + ".Enchantment-Blacklist-Enchants")))
                .replace("{glowRange}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Radius-of-glowing-" + enchantmentLevel)))
                .replace("{time}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Time-underwater-" + enchantmentLevel)))
                .replace("{damage}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Hunter-Damage-Amount-" + enchantmentLevel)))
                .replace("{speedLvl}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Rush-Speed-Amount-" + enchantmentLevel)))
                .replace("{speedTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Rush-Speed-Timer-" + enchantmentLevel)))
                .replace("{block}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Propel-Height-Amount-" + enchantmentLevel)))
                .replace("{freezingTimer}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FreezingShot-Freeze-Timer-" + enchantmentLevel)))
                .replace("{healthTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".HealthBar-Timer-" + enchantmentLevel)))
                .replace("{extraHearts}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Growth-Heart-Increase-" + enchantmentLevel)))
                .replace("{poisonTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-Poison-Timer-" + enchantmentLevel)))
                .replace("{poisonLevel}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-Poison-Level-" + enchantmentLevel)))
                .replace("{stunTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Stunning-Stun-Timer-" + enchantmentLevel)))
                .replace("{stunLevel}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Stunning-Stun-Levels-" + enchantmentLevel)))
                .replace("{iceTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".IceAspect-Frozen-Timer-" + enchantmentLevel)))
                .replace("{extractorMultiplier}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Extractor-EXP-Multiplier-" + enchantmentLevel)))
                .replace("{healingAmount}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".EnergyAbsorption-Healing-Amount-" + enchantmentLevel)))
                .replace("{healingStartAmount}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".EnergyAbsorption-Start-Healing-Amount-" + enchantmentLevel)))
                .replace("{wingsDamageReduction}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".WingsOfAegis-Protection-Percentage-" + enchantmentLevel)))
                .replace("{AsclepiusHearts}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Asclepius-HealthBoost-" + enchantmentLevel) * 2))
                .replace("{decapitateChance}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Decapitate-Proc-Chance-" + enchantmentLevel) * 100))
                .replace("{finalGuardProtectionPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FinalGuard-Protection-Percent-" + enchantmentLevel) * 100))
                .replace("{finalGuardRepairPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FinalGuard-Repair-Percent-" + enchantmentLevel) * 100))
                .replace("{leechPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Leech-Healing-Amount-Percent-" + enchantmentLevel) * 100)));

        ArrayList<String> enchantmentLore = new ArrayList<>();
        List<String> loreList = main.getEnchantmentsConfig().getStringList("Enchantments." + enchantmentName + ".Enchantment-Lore");
        for (String lore : loreList) {
            String withPAPISet1 = main.setPlaceholders(p, lore);
            enchantmentLore.add(Main.color(withPAPISet1)
                    .replace("{lvl}", String.valueOf(enchantmentLevel))
                    .replace("{blacklistEnchant}", String.valueOf(main.getEnchantmentsConfig().getStringList("Enchantments." + enchantmentName + ".Enchantment-Blacklist-Enchants")))
                    .replace("{glowRange}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Radius-of-glowing-" + enchantmentLevel)))
                    .replace("{time}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Time-underwater-" + enchantmentLevel)))
                    .replace("{damage}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Hunter-Damage-Amount-" + enchantmentLevel)))
                    .replace("{speedLvl}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Rush-Speed-Amount-" + enchantmentLevel)))
                    .replace("{speedTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Rush-Speed-Timer-" + enchantmentLevel)))
                    .replace("{block}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Propel-Height-Amount-" + enchantmentLevel)))
                    .replace("{freezingTimer}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FreezingShot-Freeze-Timer-" + enchantmentLevel)))
                    .replace("{healthTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".HealthBar-Timer-" + enchantmentLevel)))
                    .replace("{extraHearts}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Growth-Heart-Increase-" + enchantmentLevel)))
                    .replace("{poisonTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-Poison-Timer-" + enchantmentLevel)))
                    .replace("{poisonLevel}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".PoisonAspect-Poison-Level-" + enchantmentLevel)))
                    .replace("{stunTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Stunning-Stun-Timer-" + enchantmentLevel)))
                    .replace("{stunLevel}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Stunning-Stun-Levels-" + enchantmentLevel)))
                    .replace("{iceTimer}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".IceAspect-Frozen-Timer-" + enchantmentLevel)))
                    .replace("{extractorMultiplier}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Extractor-EXP-Multiplier-" + enchantmentLevel)))
                    .replace("{healingAmount}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".EnergyAbsorption-Healing-Amount-" + enchantmentLevel)))
                    .replace("{healingStartAmount}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".EnergyAbsorption-Start-Healing-Amount-" + enchantmentLevel)))
                    .replace("{wingsDamageReduction}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".WingsOfAegis-Protection-Percentage-" + enchantmentLevel)))
                    .replace("{AsclepiusHearts}", String.valueOf(main.getEnchantmentsConfig().getInt("Enchantments." + enchantmentName + ".Asclepius-HealthBoost-" + enchantmentLevel) * 2))
                    .replace("{decapitateChance}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Decapitate-Proc-Chance-" + enchantmentLevel) * 100))
                    .replace("{finalGuardProtectionPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FinalGuard-Protection-Percent-" + enchantmentLevel) * 100))
                    .replace("{finalGuardRepairPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".FinalGuard-Repair-Percent-" + enchantmentLevel) * 100))
                    .replace("{leechPercent}", String.valueOf(main.getEnchantmentsConfig().getDouble("Enchantments." + enchantmentName + ".Leech-Healing-Amount-Percent-" + enchantmentLevel) * 100)));
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
