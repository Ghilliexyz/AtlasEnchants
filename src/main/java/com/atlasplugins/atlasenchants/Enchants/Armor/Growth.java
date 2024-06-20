package com.atlasplugins.atlasenchants.Enchants.Armor;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class Growth implements Listener
{
    private Main main;
    public Growth (Main main) {
        this.main = main;
    }

    public boolean hasHelmet (Player p) {
        // Get the player's helmet item
        ItemStack armor = p.getInventory().getHelmet();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getConfig().getStringList("Enchantments.GROWTH.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    public boolean hasChestplate (Player p) {
        // Get the player's helmet item
        ItemStack helmet = p.getInventory().getChestplate();

        // Get the list of items the Enchant can be applied to from the config
        List<String> helmetMat = main.getConfig().getStringList("Enchantments.GROWTH.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return helmet != null && helmetMat.contains(helmet.getType().toString());
    }

    public boolean hasLeggings (Player p) {
        // Get the player's helmet item
        ItemStack helmet = p.getInventory().getLeggings();

        // Get the list of items the Enchant can be applied to from the config
        List<String> helmetMat = main.getConfig().getStringList("Enchantments.GROWTH.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return helmet != null && helmetMat.contains(helmet.getType().toString());
    }

    public boolean hasBoots (Player p) {
        // Get the player's helmet item
        ItemStack helmet = p.getInventory().getBoots();

        // Get the list of items the Enchant can be applied to from the config
        List<String> helmetMat = main.getConfig().getStringList("Enchantments.GROWTH.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return helmet != null && helmetMat.contains(helmet.getType().toString());
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        p.sendMessage(Main.color("&aPlayer moved"));
        // Check if the player has an enchanted sword
        if(hasHelmet(p) || hasChestplate(p) || hasLeggings(p) || hasBoots(p)) {
            PersistentDataContainer enchantedItemPDC = null;
            if (hasHelmet(p))
            {
                enchantedItemPDC = p.getInventory().getHelmet().getItemMeta().getPersistentDataContainer();
                p.sendMessage(Main.color("&2Player Helmet Found"));
            }
            if (hasChestplate(p))
            {
                enchantedItemPDC = p.getInventory().getChestplate().getItemMeta().getPersistentDataContainer();
                p.sendMessage(Main.color("&2Player Chestplate Found"));
            }
            if (hasLeggings(p))
            {
                enchantedItemPDC = p.getInventory().getLeggings().getItemMeta().getPersistentDataContainer();
                p.sendMessage(Main.color("&2Player Leggings Found"));
            }
            if (hasBoots(p))
            {
                enchantedItemPDC = p.getInventory().getBoots().getItemMeta().getPersistentDataContainer();
                p.sendMessage(Main.color("&2Player Boots Found"));
            }
            if(enchantedItemPDC == null){return;}
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

                        if (enchantName.contains("GROWTH"))
                        {
                            // PUT ENCHANT LOGIC HERE

                            // Get extra health from the configuration
                            double extraConfigHealth = main.getConfig().getDouble("Enchantments.GROWTH.Growth-Heart-Increase-" + enchantLevel);

                            double extraHealth = 0;

                            if (hasHelmet(p)) {
                                extraHealth += extraConfigHealth;
                                p.sendMessage(Main.color("&aHelmet equipped. Extra health: " + extraConfigHealth));
                            }

                            if (hasChestplate(p)) {
                                extraHealth += extraConfigHealth;
                                p.sendMessage(Main.color("&aChestplate equipped. Extra health: " + extraConfigHealth));
                            }

                            if (hasLeggings(p)) {
                                extraHealth += extraConfigHealth;
                                p.sendMessage(Main.color("&aLeggings equipped. Extra health: " + extraConfigHealth));
                            }

                            if (hasBoots(p)) {
                                extraHealth += extraConfigHealth;
                                p.sendMessage(Main.color("&aBoots equipped. Extra health: " + extraConfigHealth));
                            }

                            // Total extra health
                            p.sendMessage(Main.color("&aTotal extra health: " + extraHealth));

                            double playersMaxHealth = p.getHealth() + (extraHealth * 2);

                            p.setMaxHealth(playersMaxHealth);
                            p.setHealth(playersMaxHealth);

                            p.sendMessage(Main.color("&aPlayers health: " + playersMaxHealth));

                            // END ENCHANT LOGIC

                        }
                    }
                    else
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
