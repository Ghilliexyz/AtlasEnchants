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

public class CreateCircesEmber implements Listener {

    private Main main;

    public CreateCircesEmber(Main main) {
        this.main = main;
    }

    public ItemStack CreateCircesEmberItem(int emberAmount, Player p) {
        ItemStack ember = new ItemStack(Main.getMaterial(main.getEnchantmentsConfig().getString("CircesEmber.CircesEmber-Item"), Material.BLAZE_POWDER));
        ItemMeta emberMeta = ember.getItemMeta();

        String displayName = main.getEnchantmentsConfig().getString("CircesEmber.CircesEmber-DisplayName");
        if (p != null) {
            displayName = main.setPlaceholders(p, displayName);
        }
        emberMeta.setDisplayName(Main.color(displayName));

        ArrayList<String> enchantmentLore = new ArrayList<>();
        List<String> loreList = main.getEnchantmentsConfig().getStringList("CircesEmber.CircesEmber-Lore");
        for (String lore : loreList) {
            if (p != null) {
                lore = main.setPlaceholders(p, lore);
            }
            enchantmentLore.add(Main.color(lore));
        }

        boolean addGlint = main.getEnchantmentsConfig().getBoolean("CircesEmber.CircesEmber-Glint-Toggle");
        if (addGlint) {
            emberMeta.addEnchant(Enchantment.INFINITY, 1, true);
            emberMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        PersistentDataContainer pdc = emberMeta.getPersistentDataContainer();
        pdc.set(Main.customCircesEmberKeys, PersistentDataType.STRING, "circes_ember");

        emberMeta.setLore(enchantmentLore);
        ember.setItemMeta(emberMeta);

        if (p != null) {
            for (int i = 0; i < emberAmount; i++) {
                HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(ember);
                if (!remainingItems.isEmpty()) {
                    for (ItemStack item : remainingItems.values()) {
                        p.getWorld().dropItemNaturally(p.getLocation(), item);
                    }
                }
            }
        }

        return ember;
    }
}
