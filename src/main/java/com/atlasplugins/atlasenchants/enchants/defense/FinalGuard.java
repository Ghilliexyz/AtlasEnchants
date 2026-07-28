package com.atlasplugins.atlasenchants.enchants.defense;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.enchantevents.RemoveCustomEnchant;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class FinalGuard implements Listener {

    private Main main;
    public FinalGuard(Main main) {this.main = main;}

    Random random = new Random();

    /** Whether this item is one Final Guard may be applied to, per the config list. */
    private boolean isApplicable(ItemStack item) {
        if (item == null) return false;
        List<String> applicable = main.getEnchantmentsConfig().getStringList("Enchantments.FINAL-GUARD.Enchantment-Apply-Item");
        return applicable.contains(item.getType().toString());
    }

    @EventHandler
    public void onBreakItem(PlayerItemDamageEvent e) {
        if(e.isCancelled()) return;
        Player p = (Player) e.getPlayer();

        // Get the broken item
        ItemStack brokenItem = e.getItem();

        int itemDurability = brokenItem.getType().getMaxDurability() - brokenItem.getDurability();
        int itemDamage = e.getDamage();

        if(itemDurability > itemDamage) return;

        // The enchant protects the item that is about to break, so that is the only item worth
        // looking at. The old code walked the player's armour slots instead and used the first
        // non-empty one, which meant an enchanted helmet protected (and was never consumed by)
        // every tool the player owned, while a Final Guard tool did nothing whenever any armour
        // was worn.
        if (!isApplicable(brokenItem)) return;

        // Get Enchantment Enabled Status
        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.FINAL-GUARD.Enchantment-Enabled");
        // if Enchantment Enabled = false return.
        if(!isEnchantmentEnabled) return;

        for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(brokenItem)) {
            if (enchant.name.contains("FINAL-GUARD")) {
                // PUT ENCHANT LOGIC HERE

                // Get the enchantments protection Success Chance
                double protectionChance = main.getEnchantmentsConfig().getDouble("Enchantments.FINAL-GUARD.FinalGuard-ProtectionChance-" + enchant.level);

                // Check if the protection Chance is less than the random double and if not then return
                if(random.nextDouble() > protectionChance) return;

                // Get the enchantments repair amount
                double repairAmount = main.getEnchantmentsConfig().getDouble("Enchantments.FINAL-GUARD.FinalGuard-RepairPercent-" + enchant.level);

                // Get the items max durability
                int maxDurability = brokenItem.getType().getMaxDurability();

                // Calculate the durability that remains (which is inverted)
                int newDurability = (int) ((1 - repairAmount) * maxDurability);

                // Ensure the new durability does not exceed the maximum durability
                newDurability = Math.min(maxDurability, newDurability);

                // Set the new Durability
                brokenItem.setDurability((short) newDurability);

                // Play Success sound
                Sound finalGuardSuccessSound;
                try {
                    finalGuardSuccessSound = Main.getSound(main.getEnchantmentsConfig().getString("Enchantments.FINAL-GUARD.Sound-Settings.Sound"));
                } catch (IllegalArgumentException ex) {
                    finalGuardSuccessSound = null;
                }
                float finalGuardSuccessVolume = (float) main.getEnchantmentsConfig().getDouble("Enchantments.FINAL-GUARD.Sound-Settings.Volume");
                float finalGuardSuccessPitch = (float) main.getEnchantmentsConfig().getDouble("Enchantments.FINAL-GUARD.Sound-Settings.Pitch");

                // Get the bool to check if the user wants to play the Enchantment Disabled sound
                boolean finalGuardSuccessPlaySound = main.getEnchantmentsConfig().getBoolean("Enchantments.FINAL-GUARD.Sound-Settings.Toggle");

                // check if the user doesn't want to play the sound then return if not.
                if(finalGuardSuccessPlaySound && finalGuardSuccessSound != null){
                    p.playSound(p.getLocation(), finalGuardSuccessSound, finalGuardSuccessVolume, finalGuardSuccessPitch);
                }

                // Remove an enchant from an item by calling RemoveCustomEnchant and call the method
                RemoveCustomEnchant removeCustomEnchant = new RemoveCustomEnchant(main);
                removeCustomEnchant.RemoveEnchantment(brokenItem, enchant.name);

                e.setCancelled(true);

                // The enchant has been spent and stripped, so stop - carrying on would re-read
                // stale entries from the list we just invalidated.
                break;
                //END ENCHANT LOGIC
            }
        }
    }
}
