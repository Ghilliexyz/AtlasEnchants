package com.atlasplugins.atlasenchants.enchants.weapons;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Leech implements Listener
{
    private Main main;
    public Leech (Main main) {
        this.main = main;
    }

    public boolean hasWeapon (Player p)
    {
        // Get the player's sword item
        ItemStack weapon = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> weaponMat = main.getEnchantmentsConfig().getStringList("Enchantments.LEECH.Enchantment-Apply-Item");

        // Check if the player is wearing an applicable sword
        return weapon != null && weaponMat.contains(weapon.getType().toString());
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent e)
    {
        if(e.isCancelled()) return;
        if(!(e.getDamager() instanceof Player)) {return;}

        Player p = (Player) e.getDamager();

        // Check if the player has an enchanted sword
        if(hasWeapon(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.LEECH.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
                if (enchant.name.contains("LEECH")) {
                    //PUT ENCHANT LOGIC HERE
                    if (e.getEntity() instanceof LivingEntity) {
                        double healingPercent = main.getEnchantmentsConfig().getDouble("Enchantments.LEECH.Leech-HealPercent-" + enchant.level);

                        double playerCurrentHealth = p.getHealth();
                        double damageDealt = e.getDamage();

                        double healingPlayerAmount = (healingPercent * damageDealt);

                        double healing = playerCurrentHealth + healingPlayerAmount;

                        double clampedHealth = Math.min(healing, p.getMaxHealth());

                        p.setHealth(clampedHealth);
                    }
                    //END ENCHANT LOGIC
                }
            }
        }
    }
}
