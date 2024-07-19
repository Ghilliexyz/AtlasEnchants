package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
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
                                EntityType entityType = deadEntity.getType();
                                List<ItemStack> drops = e.getDrops();


                                if(deadEntity instanceof Player){
                                    drops.add(CreatePlayerHead(entityType, p));
                                }else if(deadEntity instanceof Mob) {
                                    drops.add(CreateMobHead(entityType));
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

    private ItemStack CreatePlayerHead(EntityType entityType, Player player) {
        if (entityType.equals(EntityType.PLAYER)) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta playerHeadMeta = (SkullMeta) playerHead.getItemMeta();

            assert playerHeadMeta != null;
            playerHeadMeta.setDisplayName("SKULL OF " + player.getName());

            playerHead.setItemMeta(playerHeadMeta);
            return playerHead;
        }else{
            return null;
        }
    }

    private ItemStack CreateMobHead(EntityType entityType) {
        ItemStack mobHead = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) mobHead.getItemMeta();

        switch (entityType) {
            case CHICKEN:
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer("MHF_Chicken"));
                skullMeta.setDisplayName("Skull of " + entityType.name());
                break;
            case COW:
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer("MHF_Cow"));
                skullMeta.setDisplayName("Skull of " + entityType.name());
                break;
            case PIG:
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer("MHF_Pig"));
                skullMeta.setDisplayName("Skull of " + entityType.name());
                break;
            // Add cases for other entities as needed
            default:
                return null;
        }

        mobHead.setItemMeta(skullMeta);
        return mobHead;
    }
}
