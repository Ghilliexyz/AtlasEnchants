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

public class CreateScrapOfCirce implements Listener {

    private Main main;
    private final Random random = new Random();

    public CreateScrapOfCirce(Main main) {
        this.main = main;
    }

    public ItemStack CreateScrapOfCirceItem(int scrapAmount, Player p) {
        ItemStack scrap = new ItemStack(Material.valueOf(main.getEnchantmentsConfig().getString("ScrapOfCirceWeave.ScrapOfCirceWeave-Item")));
        ItemMeta scrapMeta = scrap.getItemMeta();

        String displayName = main.getEnchantmentsConfig().getString("ScrapOfCirceWeave.ScrapOfCirceWeave-DisplayName");
        String withPAPISet = main.setPlaceholders(p, displayName);
        scrapMeta.setDisplayName(Main.color(withPAPISet)
                .replace("{ScrapOfCirceWeaveChance}", String.valueOf(main.getEnchantmentsConfig().getDouble("ScrapOfCirceWeave.ScrapOfCirceWeave-ReturnEnchant-Chance") * 100)));

        ArrayList<String> enchantmentLore = new ArrayList<>();
        List<String> loreList = main.getEnchantmentsConfig().getStringList("ScrapOfCirceWeave.ScrapOfCirceWeave-Lore");
        for (String lore : loreList) {
            String withPAPISet1 = main.setPlaceholders(p, lore);
            enchantmentLore.add(Main.color(withPAPISet1)
                    .replace("{ScrapOfCirceWeaveChance}", String.valueOf(main.getEnchantmentsConfig().getDouble("ScrapOfCirceWeave.ScrapOfCirceWeave-ReturnEnchant-Chance") * 100)));
        }

        boolean addGlint = main.getEnchantmentsConfig().getBoolean("ScrapOfCirceWeave.ScrapOfCirceWeave-Glint-Toggle");
        if(addGlint)
        {
            // Add Glint effect
            scrapMeta.addEnchant(Enchantment.INFINITY, 1, true);
            scrapMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        int scrapofcirceID = random.nextInt();

        PersistentDataContainer pdc = scrapMeta.getPersistentDataContainer();
//        pdc.set(Main.customscrapKeys, PersistentDataType.STRING, "scrap" + ":" + scrapofcirceID);
        pdc.set(Main.customScrapOfCirceKeys, PersistentDataType.STRING, "scrapeofcirce_book");

        scrapMeta.setLore(enchantmentLore);
        scrap.setItemMeta(scrapMeta);

        // Add items to player's inventory if player is not null
        if (p != null) {
            for (int i = 0; i < scrapAmount; i++) {
                // Check if there's space in the player's inventory
                HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(scrap);

                // If the inventory is full and the item could not be added, drop it at the player's feet
                if (!remainingItems.isEmpty()) {
                    for (ItemStack item : remainingItems.values()) {
                        p.getWorld().dropItemNaturally(p.getLocation(), item);
                    }
                }
            }
        }

        return scrap;
    }
}
