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

public class CreateCustomEnchant implements Listener {

    private Main main;
    private final Random random = new Random();

    public CreateCustomEnchant(Main main) {
        this.main = main;
    }

    public ItemStack CreateCustomEnchantmentItem(String enchantmentName, int enchantmentLevel, int enchantmentAmount, Player p) {
        ItemStack enchant = new ItemStack(Material.valueOf(main.getSettingsConfig().getString("EnchantItems.EnchantItem")));
        ItemMeta enchantMeta = enchant.getItemMeta();

        String displayName = main.getEnchantmentsConfig().getString("Enchantments." + enchantmentName + ".Enchantment-Title");
        String withPAPISet = main.setPlaceholders(p, displayName);
        assert enchantMeta != null;
        enchantMeta.setDisplayName(main.applyPlaceholders(Main.color(withPAPISet), main, enchantmentName, enchantmentLevel));

        ArrayList<String> enchantmentLore = new ArrayList<>();
        List<String> loreList = main.getEnchantmentsConfig().getStringList("Enchantments." + enchantmentName + ".Enchantment-Lore");
        for (String lore : loreList) {
            String withPAPISet1 = main.setPlaceholders(p, lore);
            enchantmentLore.add(main.applyPlaceholders(Main.color(withPAPISet1), main, enchantmentName, enchantmentLevel));
        }

            boolean addGlint = main.getSettingsConfig().getBoolean("EnchantItems.EnchantItem-Glint-Toggle");
        if(addGlint)
        {
            // Add Glint effect
            enchantMeta.addEnchant(Enchantment.INFINITY, 1, true);
            enchantMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        int enchantID = random.nextInt();

        PersistentDataContainer pdc = enchantMeta.getPersistentDataContainer();
        pdc.set(Main.customEnchantKeys, PersistentDataType.STRING, enchantmentName + ":" + enchantmentLevel + ":" + enchantID);

        enchantMeta.setLore(enchantmentLore);
        enchant.setItemMeta(enchantMeta);

        // Add items to player's inventory if player is not null
        if (p != null) {
            for (int i = 0; i < enchantmentAmount; i++) {
                // Check if there's space in the player's inventory
                HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(enchant);

                // If the inventory is full and the item could not be added, drop it at the player's feet
                if (!remainingItems.isEmpty()) {
                    for (ItemStack item : remainingItems.values()) {
                        p.getWorld().dropItemNaturally(p.getLocation(), item);
                    }
                }
            }
        }

        return enchant;
    }
}
