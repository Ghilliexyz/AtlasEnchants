package com.atlasplugins.atlasenchants.Enchants.Weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Stunning implements Listener {
    private Main main;
    public Stunning (Main main) {
        this.main = main;
    }

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getConfig().getStringList("Enchantments.STUNNING.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e)
    {
        if(!(e.getDamager() instanceof Player)) {return;}

        Player p = (Player) e.getDamager();

        Entity entity = e.getEntity();

        // Check if the player has an enchanted sword
        if(hasWeapon(p)) {
            PersistentDataContainer enchantedItemPDC = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
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

                        if (enchantName.contains("STUNNING")) {

                            //PUT ENCHANT LOGIC HERE
                            if (entity instanceof LivingEntity) {
                                int poisonTimer = main.getConfig().getInt("Enchantments.STUNNING.Stunning-Stun-Timer-" + enchantLevel);
                                int poisonLevel = main.getConfig().getInt("Enchantments.STUNNING.Stunning-Stun-Levels-" + enchantLevel);

                                poisonTimer = poisonTimer * 20;

                                // Create the Slowness potion effect
                                PotionEffect potionTypeSlowness = new PotionEffect(PotionEffectType.getByName("SLOWNESS"), poisonTimer, poisonLevel, true, true, true);
                                // Create the Weakness potion effect
                                PotionEffect potionTypeWeakness = new PotionEffect(PotionEffectType.WEAKNESS, poisonTimer, poisonLevel, true, true, true);

                                // Apply the potion effect to the entity
                                ((LivingEntity) entity).addPotionEffect(potionTypeSlowness);
                                ((LivingEntity) entity).addPotionEffect(potionTypeWeakness);
                            }
                            //END ENCHANT LOGIC
                        }
                    }else {
                        // Handle unexpected format
                        System.out.println("Unexpected enchantment format: " + enchantment);
                    }
                }
            } else {
                System.out.println("No enchantments found on the item.");
            }
        }
    }
}
