package com.atlasplugins.atlasenchants.enchants.defense;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class EnergyAbsorption implements Listener {
    private Main main;
    public EnergyAbsorption (Main main) {
        this.main = main;
    }

    public boolean hasDefenseMainHand(Player p) {
        // Get the items in the main hand
        ItemStack mainHandWeapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.ENERGY-ABSORPTION.Enchantment-Apply-Item");

        // Check if the player is holding an applicable item in their hand

        return weaponMat.contains(mainHandWeapon.getType().toString());
    }
    public boolean hasDefenseOffHand(Player p) {
        // Get the items in the off hand
        ItemStack offHandWeapon = p.getInventory().getItemInOffHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.ENERGY-ABSORPTION.Enchantment-Apply-Item");

        // Check if the player is holding an applicable item in their hand

        return weaponMat.contains(offHandWeapon.getType().toString());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e)
    {
        if(!(e.getEntity() instanceof Player)) {return;}

        Player p = (Player) e.getEntity();

        // Check if the player has an enchanted sword
        if(hasDefenseMainHand(p) || hasDefenseOffHand(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.ENERGY-ABSORPTION.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;
            // if the player is not blocking return.
            if(!p.isBlocking()) return;

            PersistentDataContainer enchantedItemPDC;
            if(hasDefenseMainHand(p))
            {
                enchantedItemPDC = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
            }else {
                enchantedItemPDC = p.getInventory().getItemInOffHand().getItemMeta().getPersistentDataContainer();
            }
            String enchantedItemData = enchantedItemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

            // Ensure the enchantment data is not null or empty
            if (enchantedItemData != null && !enchantedItemData.isEmpty()) {
                String[] enchantments = enchantedItemData.split(",");

                for (String enchantment : enchantments) {
                    String[] enchantParts = enchantment.split(":");

                    // Ensure the format is correct
                    if (enchantParts.length == 2)
                    {
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);

                        if (enchantName.contains("ENERGY-ABSORPTION")) {
                            //PUT ENCHANT LOGIC HERE

                            // get the amount needed before the healing can begin
                            double startHealingAmount = main.getEnchantmentsConfig().getDouble("Enchantments.ENERGY-ABSORPTION.EnergyAbsorption-Start-Healing-Amount-" + enchantLevel);

                            // if the players health is greater than 3 return.
                            if(p.getHealth() > (startHealingAmount * 2)) return;

                            // get healing amount
                            double healingAmount = main.getEnchantmentsConfig().getDouble("Enchantments.ENERGY-ABSORPTION.EnergyAbsorption-Healing-Amount-" + enchantLevel);

                            // get the players current health
                            double playersCurrentHealth = p.getHealth();

                            // set the players health to its current health and increase it by the healing amount
                            p.setHealth(playersCurrentHealth + (healingAmount * 2));

                            // Get the location of the dead entity
                            Location entityLoc = p.getLocation();

                            // Particle Settings Controlled Via Config
                            // Get the bool to see if the user wants to display the particles
                            boolean useParticles = main.getEnchantmentsConfig().getBoolean("Enchantments.ENERGY-ABSORPTION.EnergyAbsorption-Particle-Settings.EnergyAbsorption-Particle-Toggle");
                            // Get the Particle 1 Name
                            Particle particle1Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.ENERGY-ABSORPTION.EnergyAbsorption-Particle-Settings.EnergyAbsorption-Particle-1.EnergyAbsorption-Particle-Name-1"));
                            // Get the Particle 1 Amount
                            int particle1Amount = main.getEnchantmentsConfig().getInt("Enchantments.ENERGY-ABSORPTION.EnergyAbsorption-Particle-Settings.EnergyAbsorption-Particle-1.EnergyAbsorption-Particle-Amount-1");
                            // Get the Particle 1 Size
                            float particle1Size = (float) main.getEnchantmentsConfig().getDouble("Enchantments.ENERGY-ABSORPTION.EnergyAbsorption-Particle-Settings.EnergyAbsorption-Particle-1.EnergyAbsorption-Particle-Size-1");
                            // Get the Particle 2 Name

                            if(useParticles)
                            {
                                // Spawn particle effect
                                p.getWorld().spawnParticle(particle1Name, entityLoc, particle1Amount, 1, 1, 1, particle1Size);
                            }
                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }
}

