package com.atlasplugins.atlasenchants.enchants.weapons;

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

public class Leech implements Listener
{
    private Main main;
    public Leech (Main main) {
        this.main = main;
    }

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.LEECH.Enchantment-Apply-Item");

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

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.LEECH.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

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

                        if (enchantName.contains("LEECH")) {
                            //PUT ENCHANT LOGIC HERE
                            if (e.getEntity() instanceof LivingEntity) {
                                double healingPercent = main.getEnchantmentsConfig().getDouble("Enchantments.LEECH.Leech-Healing-Amount-Percent-" + enchantLevel);

                                double playerCurrentHealth = p.getHealth();
                                double damageDealt = e.getDamage();

//                                double healingPlayerAmount = (healingPercent / 100 * damageDealt);
                                double healingPlayerAmount = (healingPercent * damageDealt);

                                System.out.println(healingPlayerAmount);

                                double healing = playerCurrentHealth + healingPlayerAmount;

                                double clampedHealth = Math.min(healing, p.getMaxHealth());

                                p.setHealth(clampedHealth);
                            }
                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }
}
