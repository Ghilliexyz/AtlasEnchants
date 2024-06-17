package com.atlasplugins.atlasenchants.Enchants.Weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Death implements Listener
{
    private Main main;
    public Death (Main main) {
        this.main = main;
    }

    public boolean hasWeapon (Player p) {
        // Get the player's sword item
        ItemStack sword = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> swordMat = main.getConfig().getStringList("Enchantments.DEATH.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return sword != null && swordMat.contains(sword.getType().toString());
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e)
    {
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
                    if (enchantParts.length == 2) {
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);

                        double damageMultiplier = main.getConfig().getDouble("Enchantments.DEATH.Death-Damage-Amount-" + enchantLevel);

                        if (enchantName.contains("DEATH"))
                        {
                            //PUT ENCHANT LOGIC HERE
                            if(e.getEntity() instanceof Monster)
                            {
                                e.setDamage(damageMultiplier);
                            }
                            //END ENCHANT LOGIC
                        }
                    } else {
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
