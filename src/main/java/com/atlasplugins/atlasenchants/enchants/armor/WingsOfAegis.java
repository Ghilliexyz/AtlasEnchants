package com.atlasplugins.atlasenchants.enchants.armor;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class WingsOfAegis implements Listener {

    private final Main main;

    // Damage causes that bypass armor and protection in vanilla
    private static final Set<DamageCause> BYPASSED_CAUSES = EnumSet.of(
            DamageCause.VOID,
            DamageCause.KILL,
            DamageCause.SUICIDE,
            DamageCause.STARVATION,
            DamageCause.CUSTOM,
            DamageCause.SONIC_BOOM
    );

    public WingsOfAegis(Main main) {
        this.main = main;
    }

    public boolean hasArmor(Player p) {
        ItemStack armor = p.getInventory().getChestplate();
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.WINGS-OF-AEGIS.Enchantment-Apply-Item");
        return armor != null && armorMat.contains(armor.getType().toString());
    }

    @EventHandler
    public void onPlayerHit(EntityDamageEvent e) {
        if(e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getEntity();

        if (!hasArmor(player)) return;

        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.WINGS-OF-AEGIS.Enchantment-Enabled");
        if (!isEnchantmentEnabled) return;

        // Skip damage causes that bypass armor/protection in vanilla
        if (BYPASSED_CAUSES.contains(e.getCause())) return;

        for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(player.getInventory().getChestplate())) {
            if (enchant.name.contains("WINGS-OF-AEGIS")) {
                double defensePoints = main.getEnchantmentsConfig().getDouble("Enchantments.WINGS-OF-AEGIS.WingsOfAegis-Armor-Defense-Points");
                double toughness = main.getEnchantmentsConfig().getDouble("Enchantments.WINGS-OF-AEGIS.WingsOfAegis-Armor-Toughness");
                double damage = e.getDamage();

                // Step 1: Vanilla armor defense formula
                double armorReduction = Math.min(20.0, Math.max(defensePoints / 5.0, defensePoints - (4.0 * damage) / (2.0 + toughness)));
                double damageAfterArmor = damage * (1.0 - armorReduction / 25.0);

                // Step 2: Vanilla Protection enchantment formula
                int epf = enchant.level;
                double damageAfterProtection = damageAfterArmor * (1.0 - Math.min(20, epf) / 25.0);

                e.setDamage(damageAfterProtection);
            }
        }
    }
}
