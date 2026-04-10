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
import org.bukkit.inventory.meta.Damageable;

import java.util.List;
import java.util.Random;

public class FinalGuard implements Listener {

    private Main main;
    public FinalGuard(Main main) {this.main = main;}

    Random random = new Random();

    public boolean hasArmor (Player p) {
        // Get the player's helmet item
        ItemStack armor = null;

        if(p.getInventory().getHelmet() != null)
        {
            armor = p.getInventory().getHelmet();
        }
        if(p.getInventory().getChestplate() != null)
        {
            armor = p.getInventory().getChestplate();
        }
        if(p.getInventory().getLeggings() != null)
        {
            armor = p.getInventory().getLeggings();
        }
        if(p.getInventory().getBoots() != null)
        {
            armor = p.getInventory().getBoots();
        }

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.FINAL-GUARD.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    public boolean hasToolMainHand (Player p) {
        // Get the items in the main hand
        ItemStack armor = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.FINAL-GUARD.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable helmet
        return armor != null && armorMat.contains(armor.getType().toString());
    }
    public boolean hasToolOffHand(Player p) {
        // Get the items in the offhand
        ItemStack armor = p.getInventory().getItemInOffHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.FINAL-GUARD.Enchantment-Apply-Item");

        // Check if the player is holding an applicable item in their hand

        return armor != null && armorMat.contains(armor.getType().toString());
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

        // Check if the player has an enchanted helmet
        if (!hasArmor(p) && !hasToolMainHand(p) && !hasToolOffHand(p)) return;

        // Get Enchantment Enabled Status
        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.FINAL-GUARD.Enchantment-Enabled");
        // if Enchantment Enabled = false return.
        if(!isEnchantmentEnabled) return;

        // Find the item that has the FINAL-GUARD enchant
        ItemStack enchantedItem = null;
        if(p.getInventory().getHelmet() != null) enchantedItem = p.getInventory().getHelmet();
        else if(p.getInventory().getChestplate() != null) enchantedItem = p.getInventory().getChestplate();
        else if(p.getInventory().getLeggings() != null) enchantedItem = p.getInventory().getLeggings();
        else if(p.getInventory().getBoots() != null) enchantedItem = p.getInventory().getBoots();
        else if(hasToolMainHand(p)) enchantedItem = p.getInventory().getItemInMainHand();
        else if(hasToolOffHand(p)) enchantedItem = p.getInventory().getItemInOffHand();

        if(enchantedItem == null) return;

        for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(enchantedItem)) {
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
                    finalGuardSuccessSound = Sound.valueOf(main.getEnchantmentsConfig().getString("Enchantments.FINAL-GUARD.Sound-Settings.Sound"));
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

                //END ENCHANT LOGIC
            }
        }
    }
}
