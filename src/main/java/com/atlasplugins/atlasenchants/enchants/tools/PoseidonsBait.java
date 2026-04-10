package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateRandomCustomEnchant;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

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
        if(e.isCancelled()) return;
        Player p = e.getPlayer();

        // if the player hasn't caught a fish then return
        if(!(e.getState() == PlayerFishEvent.State.CAUGHT_FISH)) return;

        if(!(e.getCaught() instanceof Item)) return;
        Item item = (Item) e.getCaught();

        // Check if the player has an enchanted sword
        if (hasTool(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.POSEIDONS-BAIT.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if (!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
                if (enchant.name.contains("POSEIDONS-BAIT")) {
                    //PUT ENCHANT LOGIC HERE
                    double procChance = main.getEnchantmentsConfig().getDouble("Enchantments.POSEIDONS-BAIT.PoseidonsBait-ProcChance-" + enchant.level);

                    if (random.nextDouble() > procChance) return;

                    // Particle Settings Controlled Via Config
                    // Get the bool to see if the user wants to display the particles
                    boolean useParticles = main.getEnchantmentsConfig().getBoolean("Enchantments.POSEIDONS-BAIT.Particle-Settings.Toggle");
                    // Get the Particle 1 Name
                    Particle particle1Name;
                    try {
                        particle1Name = Particle.valueOf(main.getEnchantmentsConfig().getString("Enchantments.POSEIDONS-BAIT.Particle-Settings.Particle-1.Name"));
                    } catch (IllegalArgumentException ex) {
                        return;
                    }
                    // Get the Particle 1 Amount
                    int particle1Amount = main.getEnchantmentsConfig().getInt("Enchantments.POSEIDONS-BAIT.Particle-Settings.Particle-1.Amount");
                    // Get the Particle 1 Size
                    float particle1Size = (float) main.getEnchantmentsConfig().getDouble("Enchantments.POSEIDONS-BAIT.Particle-Settings.Particle-1.Size");

                    if (useParticles) {
                        // Spawn particle effect
                        p.getWorld().spawnParticle(particle1Name, p.getLocation(), particle1Amount, 1, 1, 1, particle1Size);
                    }

                    // create random custom enchant
                    CreateRandomCustomEnchant createRandomCustomEnchant = new CreateRandomCustomEnchant(main);
                    ItemStack enchantItem = createRandomCustomEnchant.CreateRandomCustomEnchantmentItem(p, 1, false, null);

                    item.setItemStack(enchantItem);
                    //END ENCHANT LOGIC
                }
            }
        }
    }
}
