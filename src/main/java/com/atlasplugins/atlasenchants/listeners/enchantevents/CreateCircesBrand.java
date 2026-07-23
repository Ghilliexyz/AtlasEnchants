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

public class CreateCircesBrand implements Listener {

    private Main main;
    private final Random random = new Random();

    public CreateCircesBrand(Main main) {
        this.main = main;
    }

    public ItemStack CreateCircesBrandItem(int brandAmount, Player p) {
        Material brandMaterial;
        try {
            brandMaterial = Material.valueOf(main.getEnchantmentsConfig().getString("CircesBrand.CircesBrand-Item", "NAME_TAG").toUpperCase());
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

        int brandID = random.nextInt();

        PersistentDataContainer pdc = brandMeta.getPersistentDataContainer();
        pdc.set(Main.customCircesBrandKeys, PersistentDataType.STRING, "circes_brand:" + brandID);

        brandMeta.setLore(brandLore);
        brand.setItemMeta(brandMeta);

        if (p != null) {
            for (int i = 0; i < brandAmount; i++) {
                HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(brand);
                if (!remainingItems.isEmpty()) {
                    for (ItemStack item : remainingItems.values()) {
                        p.getWorld().dropItemNaturally(p.getLocation(), item);
                    }
                }
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
