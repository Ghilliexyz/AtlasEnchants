package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateRandomCustomEnchant;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;

public class PoseidonsBait implements Listener {

    private Main main;
    private final Random random = new Random();
    public PoseidonsBait (Main main) {
        this.main = main;
    }

    public boolean hasTool (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.POSEIDONS-BAIT.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent e)
    {
        Player p = e.getPlayer();

        Item item = (Item) e.getCaught();

        // if the player hasn't caught a fish then return
        if(!(e.getState() == PlayerFishEvent.State.CAUGHT_FISH)) return;

        // Check if the player has an enchanted sword
        if (hasTool(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.POSEIDONS-BAIT.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if (!isEnchantmentEnabled) return;

            PersistentDataContainer enchantedItemPDC = p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer();
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

                        if (enchantName.contains("POSEIDONS-BAIT")) {
                            //PUT ENCHANT LOGIC HERE
                            double procChance = main.getEnchantmentsConfig().getDouble("Enchantments.POSEIDONS-BAIT.PoseidonsBait-Proc-Chance-" + enchantLevel);

                            if (random.nextDouble() > procChance) return;

                            // Particle Settings Controlled Via Config
                            // Get the bool to see if the user wants to display the particles
                            boolean useParticles = main.getEnchantmentsConfig().getBoolean("Enchantments.POSEIDONS-BAIT.PoseidonsBait-Particle-Settings.PoseidonsBait-Particle-Toggle");
                            // Get the Particle 1 Name
                            Particle particle1Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.POSEIDONS-BAIT.PoseidonsBait-Particle-Settings.PoseidonsBait-Particle-1.PoseidonsBait-Particle-Name-1"));
                            // Get the Particle 1 Amount
                            int particle1Amount = main.getEnchantmentsConfig().getInt("Enchantments.POSEIDONS-BAIT.PoseidonsBait-Particle-Settings.PoseidonsBait-Particle-1.PoseidonsBait-Particle-Amount-1");
                            // Get the Particle 1 Size
                            float particle1Size = (float) main.getEnchantmentsConfig().getDouble("Enchantments.POSEIDONS-BAIT.PoseidonsBait-Particle-Settings.PoseidonsBait-Particle-1.PoseidonsBait-Particle-Size-1");

                            if (useParticles) {
                                // Spawn particle effect
                                p.getWorld().spawnParticle(particle1Name, p.getLocation(), particle1Amount, 1, 1, 1, particle1Size);
                            }

                            // create random custom enchant
                            CreateRandomCustomEnchant createRandomCustomEnchant = new CreateRandomCustomEnchant(main);
                            ItemStack enchant = createRandomCustomEnchant.CreateRandomCustomEnchantmentItem(p, 1, false, null);

                            item.setItemStack(enchant);

                            // cancel event to stop the default loot being given.
//                            e.setCancelled(true);
                            //END ENCHANT LOGIC
                        }
                    }
                }
            }
        }
    }
}
