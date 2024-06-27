package com.atlasplugins.atlasenchants.Enchants.Weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Propel implements Listener
{

    private Main main;
    public Propel (Main main) {
        this.main = main;
    }

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.PROPEL.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e)
    {
        if(!(e.getDamager() instanceof Player)) {return;}

        Player p = (Player) e.getDamager();

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

                        if (enchantName.contains("PROPEL")) {
                            // PUT ENCHANT LOGIC HERE
                            if (e.getEntity() instanceof LivingEntity) {
                                // Get the block height from the configuration
                                int blockHeight = main.getEnchantmentsConfig().getInt("Enchantments.PROPEL.Propel-Height-Amount-" + enchantLevel);

                                // Get the entity to launch
                                LivingEntity entityToLaunch = (LivingEntity) e.getEntity();

                                // Add a small vertical offset to the entity's position to ensure it gets launched
                                entityToLaunch.teleport(entityToLaunch.getLocation().add(0, blockHeight, 0));
                            }
                            //END ENCHANT LOGIC
                        }
                    }else
                    {
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
