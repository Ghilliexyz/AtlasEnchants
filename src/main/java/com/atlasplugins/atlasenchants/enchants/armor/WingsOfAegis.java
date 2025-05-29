package com.atlasplugins.atlasenchants.enchants.armor;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class WingsOfAegis implements Listener {

    private Main main;
    public WingsOfAegis(Main main) {this.main = main;}

    public boolean hasArmor (Player p) {
        // Get the player's helmet item
        ItemStack armor = p.getInventory().getChestplate();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.WINGS-OF-AEGIS.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    @EventHandler
    public void onPlayerHit(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getEntity();

        // Check if the player has an enchanted helmet
        if (hasArmor(player)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.WINGS-OF-AEGIS.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            PersistentDataContainer container = player.getInventory().getChestplate().getItemMeta().getPersistentDataContainer();
            String enchantmentData = container.getOrDefault(Main.customEnchantKeys, PersistentDataType.STRING, "");

            if (!enchantmentData.isEmpty()) {
                String[] enchantments = enchantmentData.split(",");
                for (String enchantment : enchantments) {
                    String[] enchantParts = enchantment.split(":");
                    // Ensure the format is correct
                    if (enchantParts.length == 3) {
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);
                        int enchantID = Integer.parseInt(enchantParts[2]);

                        if (enchantName.contains("WINGS-OF-AEGIS")) {
                            double reduction = main.getEnchantmentsConfig().getDouble("Enchantments.WINGS-OF-AEGIS.WingsOfAegis-Protection-Percentage-" + enchantLevel);

                            double originalDamage = e.getDamage();
                            double finalDamage = originalDamage * (1 - reduction);

                            e.setDamage(finalDamage); // 🎯 Just set the new damage directly
                        }
                    }
                }
            }
        }
    }

    public double calculateReducedDamage(double reductionPercent, double originalDamage) {
        // Calculate the reduced damage
        return reductionPercent * originalDamage;
    }
}