package com.atlasplugins.atlasenchants.enchants.armor;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
// ADD crouch ability activator for different levels and different times it lasts for
// example level 1 = 5 block distance with 5 seconds of fear sight vision
// then it has 5 minute cooldown or something.
// or you have like 0.5 seconds of vision and then you get like 20-30 second cooldown so you are forced to take advantage of it.
public class Fearsight implements Listener
{

    private Main main;
    public Fearsight (Main main) {
        this.main = main;
    }

    private List<Entity> ListEntity;

    public boolean hasArmor (Player p) {
        // Get the player's helmet item
        ItemStack armor = p.getInventory().getHelmet();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.FEARSIGHT.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        // Check if the player has an enchanted helmet
        if(hasArmor(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.FEARSIGHT.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            PersistentDataContainer enchantedItemPDC = p.getInventory().getHelmet().getItemMeta().getPersistentDataContainer();
            String enchantedItemData = enchantedItemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

            // Ensure the enchantment data is not null or empty
            if (enchantedItemData != null && !enchantedItemData.isEmpty()) {
                String[] enchantments = enchantedItemData.split(",");

                for (String enchantment : enchantments) {
                    String[] enchantParts = enchantment.split(":");

                    // Ensure the format is correct
                    if (enchantParts.length == 3) {
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);
                        int enchantID = Integer.parseInt(enchantParts[2]);

                        double enchantRadius = main.getEnchantmentsConfig().getInt("Enchantments.FEARSIGHT.Radius-of-glowing-" + enchantLevel);

                        ListEntity = p.getNearbyEntities(enchantRadius, enchantRadius, enchantRadius);

                        ChatColor hostileMobColor = ChatColor.valueOf(main.getEnchantmentsConfig().getString("Enchantments.FEARSIGHT.Hostile-GLOW-Colour").toUpperCase());
                        ChatColor passiveMobColor = ChatColor.valueOf(main.getEnchantmentsConfig().getString("Enchantments.FEARSIGHT.Passive-GLOW-Colour").toUpperCase());
                        ChatColor normalMobColor = ChatColor.valueOf(main.getEnchantmentsConfig().getString("Enchantments.FEARSIGHT.Player-Villager").toUpperCase());

                        if (enchantName.contains("FEARSIGHT"))
                        {
                            //PUT ENCHANT LOGIC HERE
                            for (Entity entity : ListEntity) {
                                try {
                                    if (entity instanceof Monster || entity instanceof Flying || entity instanceof Slime || entity instanceof Boss) {
                                        main.glowingEntities.setGlowing(entity, p, hostileMobColor);
                                    } else if (entity instanceof Animals || entity instanceof Ambient || entity instanceof WaterMob) {
                                        main.glowingEntities.setGlowing(entity, p, passiveMobColor);
                                    } else if (entity instanceof Player || entity instanceof Villager || entity instanceof WanderingTrader || entity instanceof IronGolem) {
                                        main.glowingEntities.setGlowing(entity, p, normalMobColor);
                                    }

                                    // Remove glow if entity is out of range
                                    if (entity.getLocation().distance(p.getLocation()) >= enchantRadius) {
                                        main.glowingEntities.unsetGlowing(entity, p);
                                    }
                                } catch (ReflectiveOperationException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        } else {
            // If no helmet is worn, remove glow from all entities
            if(ListEntity == null) { return; }
            try {
                for (Entity removeGlow : ListEntity) {
                    main.glowingEntities.unsetGlowing(removeGlow, p);
                }
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent e) {
        Player player = e.getEntity();
        // Remove glow effect when the player dies
        if(ListEntity == null) { return; }
        for (Entity entity : ListEntity) {
            try {
                main.glowingEntities.unsetGlowing(entity, player);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @EventHandler
    public void PlayerLeaveEvent(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        // Remove glow effect when the player leaves the game
        if(ListEntity == null) { return; }
        for (Entity entity : ListEntity) {
            try {
                main.glowingEntities.unsetGlowing(entity, player);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
