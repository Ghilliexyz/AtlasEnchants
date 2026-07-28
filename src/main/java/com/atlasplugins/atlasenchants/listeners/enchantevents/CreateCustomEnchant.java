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

    /**
     * Builds {@code enchantmentAmount} books and, when {@code p} is non-null, hands them over.
     *
     * <p>Each book is built separately so it gets its own enchant ID. Building one book and adding
     * it repeatedly produced identical meta, which meant the books <em>stacked</em> - and applying
     * a stack consumed all of it for a single enchant.
     *
     * @return one of the created books (with amount 1), for callers that only want the item.
     */
    public ItemStack CreateCustomEnchantmentItem(String enchantmentName, int enchantmentLevel, int enchantmentAmount, Player p) {
        ItemStack last = buildEnchantmentBook(enchantmentName, enchantmentLevel, p);

        if (p != null) {
            for (int i = 0; i < enchantmentAmount; i++) {
                // A fresh book per iteration, so no two carry the same ID and they never stack.
                ItemStack book = (i == 0) ? last : buildEnchantmentBook(enchantmentName, enchantmentLevel, p);
                last = book;

                HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(book);

                // If the inventory is full and the item could not be added, drop it at the player's feet
                if (!remainingItems.isEmpty()) {
                    for (ItemStack item : remainingItems.values()) {
                        p.getWorld().dropItemNaturally(p.getLocation(), item);
                    }
                }
            }
        }

        return last;
    }

    /** Builds a single enchant book with its own randomly generated enchant ID. */
    private ItemStack buildEnchantmentBook(String enchantmentName, int enchantmentLevel, Player p) {
        ItemStack enchant = new ItemStack(Main.getMaterial(main.getSettingsConfig().getString("EnchantItems.EnchantItem"), Material.ENCHANTED_BOOK));
        ItemMeta enchantMeta = enchant.getItemMeta();

        String displayName = main.getEnchantmentsConfig().getString("Enchantments." + enchantmentName + ".Enchantment-Title");
        String withPAPISet = main.setPlaceholders(p, displayName);
        if (enchantMeta == null) return enchant;
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

        return enchant;
    }
}
