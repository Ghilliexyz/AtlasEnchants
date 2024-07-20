package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MinersTouch implements Listener {

    private Main main;
    public MinersTouch (Main main) {
        this.main = main;
    }

    public boolean hasTool (Player p) {
        // Get the player's tool(s)
        ItemStack tool = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.MINERS-TOUCH.Enchantment-Apply-Item");

        // Check if the player has a tool in their hand
        return tool != null && armorMat.contains(tool.getType().toString());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        //Grabbing the player
        Player p = e.getPlayer();

        // Check if the player has an enchanted tool
        if(hasTool(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.MINERS-TOUCH.Enchantment-Enabled");
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
                    if (enchantParts.length == 2) {
                        String enchantName = enchantParts[0];
                        int enchantLevel = Integer.parseInt(enchantParts[1]);

                        if (enchantName.contains("MINERS-TOUCH")) {
                            // PUT ENCHANT LOGIC HERE
                            // Get the block mined
                            Block block = e.getBlock();
                            // if block isn't an instance of CreatureSpawner return
                            if(!(block.getState() instanceof CreatureSpawner)) return;

                            // Set Block Exp to 0, so they can't gain an infinite amount of xp.
                            e.setExpToDrop(0);

                            // Get the CreatureSpawner State
                            CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();

                            // Get the SpawnerType (Cow, Pig, Zombie etc)
                            EntityType spawnerType = spawnerBlock.getSpawnedType();

                            // Create a fake spawner to drop on the ground when the spawner is mined
                            ItemStack fakeSpawner = new ItemStack(Material.SPAWNER);
                            ItemMeta fakeSpawnerMeta = fakeSpawner.getItemMeta();

                            // Set the entity type in the items Persistent data container
                            PersistentDataContainer spawnerPDC = fakeSpawnerMeta.getPersistentDataContainer();
                            spawnerPDC.set(Main.spawnerKeys, PersistentDataType.STRING, spawnerType.name());

                            // Reformat spawner name
                            String spawnerNameReformatted = reformatString(spawnerType.name());

                            // Set Spawners Name
                            String displayName = main.getEnchantmentsConfig().getString("Enchantments.MINERS-TOUCH.MinersTouch-Spawner-Title-Style");
                            String withPAPISet = main.setPlaceholders(p, displayName);
                            fakeSpawnerMeta.setDisplayName(Main.color(withPAPISet)
                                    .replace("{spawnerType}", spawnerNameReformatted));

                            // Hide Random Lore Message
                            fakeSpawnerMeta.addItemFlags(ItemFlag.values());

                            // Set Spawners Lore
                            ArrayList<String> fakeSpawnerLore = new ArrayList<>();
                            List<String> loreList = main.getEnchantmentsConfig().getStringList("Enchantments.MINERS-TOUCH.MinersTouch-Spawner-Lore-Style");
                            for (String lore : loreList) {
                                String withPAPISet1 = main.setPlaceholders(p, lore);
                                fakeSpawnerLore.add(Main.color(withPAPISet1)
                                        .replace("{spawnerType}", spawnerNameReformatted));
                            }

                            // Set the new item meta
                            fakeSpawnerMeta.setLore(fakeSpawnerLore);
                            fakeSpawner.setItemMeta(fakeSpawnerMeta);

                            // Drop the spawner item on the ground
                            block.getWorld().dropItemNaturally(block.getLocation(), fakeSpawner);

                            // Particle Settings Controlled Via Config
                            // Get the bool to see if the user wants to display the particles
                            boolean useParticles = main.getEnchantmentsConfig().getBoolean("Enchantments.MINERS-TOUCH.MinersTouch-Particle-Settings.MinersTouch-Particle-Toggle");
                            // Get the Particle 1 Name
                            Particle particle1Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.MINERS-TOUCH.MinersTouch-Particle-Settings.MinersTouch-Particle-1.MinersTouch-Particle-Name-1"));
                            // Get the Particle 1 Amount
                            int particle1Amount = main.getEnchantmentsConfig().getInt("Enchantments.MINERS-TOUCH.MinersTouch-Particle-Settings.MinersTouch-Particle-1.MinersTouch-Particle-Amount-1");
                            // Get the Particle 1 Size
                            float particle1Size = (float) main.getEnchantmentsConfig().getDouble("Enchantments.MINERS-TOUCH.MinersTouch-Particle-Settings.MinersTouch-Particle-1.MinersTouch-Particle-Size-1");

                            if(useParticles) {
                                // Update location in case entity moves
                                Location entityLoc = block.getLocation();

                                // Spawn particle effect
                                block.getWorld().spawnParticle(particle1Name, entityLoc, particle1Amount, 1, 1, 1, particle1Size);
                            }

                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }

    public static String reformatString(String input) {
        // Convert the input string to lower case and split it by underscores
        String[] parts = input.toLowerCase().split("_");

        // Create a StringBuilder to build the formatted string
        StringBuilder formatted = new StringBuilder();

        // Capitalize the first letter of each part and append to the StringBuilder
        for (String part : parts) {
            if (part.length() > 0) {
                formatted.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append(" ");
            }
        }

        // Trim the trailing space and return the formatted string
        return formatted.toString().trim();
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        // Get the player and the placed block
        Player p = e.getPlayer();
        Block block = e.getBlockPlaced();

        // Check if the placed block is a spawner
        if (block.getType() == Material.SPAWNER) {
            ItemStack item = e.getItemInHand();
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();

                // Get the stored entity type from the spawner item
                String entityTypeString = pdc.get(Main.spawnerKeys, PersistentDataType.STRING);

                if (entityTypeString != null) {
                    EntityType entityType = EntityType.valueOf(entityTypeString);

                    // Set the entity type of the placed spawner
                    CreatureSpawner spawner = (CreatureSpawner) block.getState();
                    spawner.setSpawnedType(entityType);
                    spawner.update();
                }
            }
        }
    }
}
