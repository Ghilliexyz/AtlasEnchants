package com.atlasplugins.atlasenchants.Enchants.Armor;

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
        List<String> armorMat = main.getConfig().getStringList("Enchantments.FEARSIGHT.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        // Check if the player has an enchanted helmet
        if(hasArmor(p)) {
            PersistentDataContainer enchantedItemPDC = p.getInventory().getHelmet().getItemMeta().getPersistentDataContainer();
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

                        double enchantRadius = main.getConfig().getInt("Enchantments.FEARSIGHT.Radius-of-glowing-" + enchantLevel);

                        ListEntity = p.getNearbyEntities(enchantRadius, enchantRadius, enchantRadius);

                        String hostileMobColor = main.getConfig().getString("Enchantments.FEARSIGHT.Hostile-GLOW-Colour").toUpperCase();
                        String passiveMobColor = main.getConfig().getString("Enchantments.FEARSIGHT.Passive-GLOW-Colour").toUpperCase();
                        String normalMobColor = main.getConfig().getString("Enchantments.FEARSIGHT.Player-Villager").toUpperCase();

                        if (enchantName.contains("FEARSIGHT"))
                        {
                            //PUT ENCHANT LOGIC HERE
                            for (Entity entity : ListEntity) {
                                try {
                                    if (entity instanceof Monster || entity instanceof Flying || entity instanceof Slime || entity instanceof Boss) {
                                        main.glowingEntities.setGlowing(entity, p, ChatColor.valueOf(hostileMobColor));
                                    } else if (entity instanceof Animals || entity instanceof Ambient || entity instanceof WaterMob) {
                                        main.glowingEntities.setGlowing(entity, p, ChatColor.valueOf(passiveMobColor));
                                    } else if (entity instanceof Player || entity instanceof Villager || entity instanceof WanderingTrader || entity instanceof IronGolem) {
                                        main.glowingEntities.setGlowing(entity, p, ChatColor.valueOf(normalMobColor));
                                    }

                                    // Remove glow if entity is out of range
                                    if (entity.getLocation().distance(p.getLocation()) >= main.getConfig().getInt("Enchantments.FEARSIGHT.Radius-of-glowing-" + enchantLevel)) {
                                        main.glowingEntities.unsetGlowing(entity, p);
                                    }
                                } catch (ReflectiveOperationException ex) {
                                    throw new RuntimeException(ex);
                                }
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
