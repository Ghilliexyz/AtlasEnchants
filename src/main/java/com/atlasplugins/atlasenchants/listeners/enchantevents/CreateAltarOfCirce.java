package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CreateAltarOfCirce implements Listener {

    private Main main;
    private final Random random = new Random();

    public CreateAltarOfCirce(Main main) {
        this.main = main;
    }

    public ItemStack CreateAltarOfCirceItem(int oracleAmount, Player p) {
        ItemStack oracle = new ItemStack(Material.valueOf(main.getEnchantmentsConfig().getString("AltarOfCirce.AltarOfCirce-Item")));
        ItemMeta oracleMeta = oracle.getItemMeta();

        String displayName = main.getEnchantmentsConfig().getString("AltarOfCirce.AltarOfCirce-DisplayName");
        String withPAPISet = main.setPlaceholders(p, displayName);
        oracleMeta.setDisplayName(Main.color(withPAPISet)
                .replace("{AltarOfCirceChance}", String.valueOf(main.getEnchantmentsConfig().getDouble("AltarOfCirce.AltarOfCirce-ReturnEnchant-Chance") * 100)));

        ArrayList<String> enchantmentLore = new ArrayList<>();
        List<String> loreList = main.getEnchantmentsConfig().getStringList("AltarOfCirce.AltarOfCirce-Lore");
        for (String lore : loreList) {
            String withPAPISet1 = main.setPlaceholders(p, lore);
            enchantmentLore.add(Main.color(withPAPISet1)
                    .replace("{AltarOfCirceChance}", String.valueOf(main.getEnchantmentsConfig().getDouble("AltarOfCirce.AltarOfCirce-ReturnEnchant-Chance") * 100)));
        }

        boolean addGlint = main.getEnchantmentsConfig().getBoolean("AltarOfCirce.AltarOfCirce-Glint-Toggle");
        if(addGlint)
        {
            // Add Glint effect
            oracleMeta.addEnchant(Enchantment.INFINITY, 1, true);
            oracleMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        int oracleID = random.nextInt();

        PersistentDataContainer pdc = oracleMeta.getPersistentDataContainer();
//        pdc.set(Main.customOracleKeys, PersistentDataType.STRING, "Oracle" + ":" + oracleID);
        pdc.set(Main.customAltarOfCirceKeys, PersistentDataType.STRING, "altar_of_circe");

        oracleMeta.setLore(enchantmentLore);
        oracle.setItemMeta(oracleMeta);

        // Add items to player's inventory if player is not null
        if (p != null) {
            for (int i = 0; i < oracleAmount; i++) {
                // Check if there's space in the player's inventory
                HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(oracle);

                // If the inventory is full and the item could not be added, drop it at the player's feet
                if (!remainingItems.isEmpty()) {
                    for (ItemStack item : remainingItems.values()) {
                        p.getWorld().dropItemNaturally(p.getLocation(), item);
                    }
                }
            }
        }

        return oracle;
    }
}
