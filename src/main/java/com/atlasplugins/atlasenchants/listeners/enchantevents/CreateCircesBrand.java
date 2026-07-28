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

public class CreateCircesBrand implements Listener {

    /** PDC marker shared by every Brand, so they all stack together. */
    private static final String BRAND_TAG = "circes_brand";

    private Main main;

    public CreateCircesBrand(Main main) {
        this.main = main;
    }

    public ItemStack CreateCircesBrandItem(int brandAmount, Player p) {
        Material brandMaterial;
        try {
            brandMaterial = Main.getMaterial(main.getEnchantmentsConfig().getString("CircesBrand.CircesBrand-Item", "NAME_TAG"), Material.NAME_TAG);
        } catch (IllegalArgumentException ex) {
            brandMaterial = Material.NAME_TAG;
        }

        ItemStack brand = new ItemStack(brandMaterial);
        ItemMeta brandMeta = brand.getItemMeta();

        String displayName = main.getEnchantmentsConfig().getString("CircesBrand.CircesBrand-DisplayName");
        if (p != null) {
            displayName = main.setPlaceholders(p, displayName);
        }
        brandMeta.setDisplayName(Main.color(displayName));

        ArrayList<String> brandLore = new ArrayList<>();
        List<String> loreList = main.getEnchantmentsConfig().getStringList("CircesBrand.CircesBrand-Lore");
        for (String lore : loreList) {
            if (p != null) {
                lore = main.setPlaceholders(p, lore);
            }
            brandLore.add(Main.color(lore));
        }

        boolean addGlint = main.getEnchantmentsConfig().getBoolean("CircesBrand.CircesBrand-Glint-Toggle");
        if (addGlint) {
            brandMeta.addEnchant(Enchantment.INFINITY, 1, true);
            brandMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // A constant tag value, deliberately not a per-item random ID: two Brands only stack when
        // their meta is identical, and a unique ID per item would make every Brand its own stack.
        PersistentDataContainer pdc = brandMeta.getPersistentDataContainer();
        pdc.set(Main.customCircesBrandKeys, PersistentDataType.STRING, BRAND_TAG);

        brandMeta.setLore(brandLore);
        brand.setItemMeta(brandMeta);

        if (p != null) {
            ItemStack toGive = brand.clone();
            toGive.setAmount(Math.max(1, brandAmount));

            // addItem splits across stacks itself, so one call covers any amount.
            HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(toGive);
            for (ItemStack item : remainingItems.values()) {
                p.getWorld().dropItemNaturally(p.getLocation(), item);
            }
        }

        return brand;
    }

    /** True if the stack carries the Circe's Brand PDC tag. */
    public static boolean isCircesBrand(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(Main.customCircesBrandKeys, PersistentDataType.STRING);
    }
}
