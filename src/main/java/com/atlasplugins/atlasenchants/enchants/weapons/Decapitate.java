package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;

public class Decapitate implements Listener {

    private Main main;
    public Decapitate (Main main) {
        this.main = main;
    }

    private final Random random = new Random();

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.DECAPITATE.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent e)
    {
        Player p = (Player) e.getEntity().getKiller();

        if(p == null){return;}

        // Check if the player has an enchanted sword
        if(hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.DECAPITATE.Enchantment-Enabled");
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

                        if (enchantName.contains("DECAPITATE")) {
                            //PUT ENCHANT LOGIC HERE
                            double decapitateProcChance = main.getEnchantmentsConfig().getDouble("Enchantments.DECAPITATE.Decapitate-Proc-Chance-" + enchantLevel);

                            if (random.nextDouble() < decapitateProcChance) {

                                Entity deadEntity = e.getEntity();

                                List<ItemStack> drops = e.getDrops();

                                if(deadEntity.getType() == EntityType.PIG)
                                {
                                    ItemStack pigHead = new ItemStack(Material.PLAYER_HEAD, 1);
                                    SkullMeta pigHeadMeta = (SkullMeta) pigHead.getItemMeta();
                                    pigHeadMeta.setOwningPlayer(Bukkit.getServer().getOfflinePlayer("MHF_Chicken"));
                                    pigHeadMeta.setDisplayName("YES THANK YOU!");
                                    pigHead.setItemMeta(pigHeadMeta);

                                    drops.add(pigHead);
                                }

                                // Get the location of the dead entity
                                Location entityLoc = deadEntity.getLocation();

                                // Particle Settings Controlled Via Config
                                // Get the bool to see if the user wants to display the particles
                                boolean useParticles = main.getEnchantmentsConfig().getBoolean("Enchantments.DECAPITATE.Decapitate-Particle-Settings.Decapitate-Particle-Toggle");
                                // Get the Particle 1 Name
                                Particle particle1Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.DECAPITATE.Decapitate-Particle-Settings.Decapitate-Particle-1.Decapitate-Particle-Name-1"));
                                // Get the Particle 1 Amount
                                int particle1Amount = main.getEnchantmentsConfig().getInt("Enchantments.DECAPITATE.Decapitate-Particle-Settings.Decapitate-Particle-1.Decapitate-Particle-Amount-1");
                                // Get the Particle 1 Size
                                float particle1Size = (float) main.getEnchantmentsConfig().getDouble("Enchantments.DECAPITATE.Decapitate-Particle-Settings.Decapitate-Particle-1.Decapitate-Particle-Size-1");
                                // Get the Particle 2 Name

                                if (useParticles) {
                                    // Spawn particle effect
                                    e.getEntity().getWorld().spawnParticle(particle1Name, entityLoc, particle1Amount, 1, 1, 1, particle1Size);
                                }
                            }
                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }
}
