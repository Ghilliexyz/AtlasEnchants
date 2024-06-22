package com.atlasplugins.atlasenchants.Enchants.Weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Extractor implements Listener {
    private Main main;
    public Extractor (Main main) {
        this.main = main;
    }

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getConfig().getStringList("Enchantments.EXTRACTOR.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent e)
    {
        Player p = (Player) e.getEntity().getKiller();

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

                        if (enchantName.contains("EXTRACTOR")) {
                            //PUT ENCHANT LOGIC HERE
                            double expMultiplier = main.getConfig().getDouble("Enchantments.EXTRACTOR.Extractor-EXP-Multiplier-" + enchantLevel);

                            int droppedEXP = e.getDroppedExp();

                            int finalEXP = (int) (droppedEXP * expMultiplier);

                            e.setDroppedExp(finalEXP);
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
