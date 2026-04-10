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

public class CreateCircesAnvil implements Listener {

    private Main main;
    private final Random random = new Random();

    public CreateCircesAnvil(Main main) {
        this.main = main;
    }

    public ItemStack CreateCircesAnvilItem(int anvilAmount, Player p) {
        ItemStack anvil = new ItemStack(Material.valueOf(main.getEnchantmentsConfig().getString("CircesAnvil.CircesAnvil-Item")));
        ItemMeta anvilMeta = anvil.getItemMeta();

        String displayName = main.getEnchantmentsConfig().getString("CircesAnvil.CircesAnvil-DisplayName");
        if (p != null) {
            displayName = main.setPlaceholders(p, displayName);
        }
        anvilMeta.setDisplayName(Main.color(displayName));

        ArrayList<String> enchantmentLore = new ArrayList<>();
        List<String> loreList = main.getEnchantmentsConfig().getStringList("CircesAnvil.CircesAnvil-Lore");
        for (String lore : loreList) {
            if (p != null) {
                lore = main.setPlaceholders(p, lore);
            }
            enchantmentLore.add(Main.color(lore));
        }

        boolean addGlint = main.getEnchantmentsConfig().getBoolean("CircesAnvil.CircesAnvil-Glint-Toggle");
        if (addGlint) {
            anvilMeta.addEnchant(Enchantment.INFINITY, 1, true);
            anvilMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        PersistentDataContainer pdc = anvilMeta.getPersistentDataContainer();
        pdc.set(Main.customCircesAnvilKeys, PersistentDataType.STRING, "circes_anvil");

        anvilMeta.setLore(enchantmentLore);
        anvil.setItemMeta(anvilMeta);

        if (p != null) {
            for (int i = 0; i < anvilAmount; i++) {
                HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(anvil);
                if (!remainingItems.isEmpty()) {
                    for (ItemStack item : remainingItems.values()) {
                        p.getWorld().dropItemNaturally(p.getLocation(), item);
                    }
                }
            }
        }

        return anvil;
    }
}
