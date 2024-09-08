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

public class CreateShard implements Listener {

    private Main main;
    private final Random random = new Random();

    public CreateShard(Main main) {
        this.main = main;
    }

    public ItemStack CreateShardItem(int shardAmount, Player p) {
        ItemStack shard = new ItemStack(Material.valueOf(main.getSettingsConfig().getString("OblivionShard.OblivionShard-Item")));
        ItemMeta shardMeta = shard.getItemMeta();

        String displayName = main.getSettingsConfig().getString("OblivionShard.OblivionShard-DisplayName");
        String withPAPISet = main.setPlaceholders(p, displayName);
        shardMeta.setDisplayName(Main.color(withPAPISet)
                .replace("{oblivionShardChance}", String.valueOf(main.getSettingsConfig().getDouble("OblivionShard.OblivionShard-ReturnEnchant-Chance") * 100)));

        ArrayList<String> enchantmentLore = new ArrayList<>();
        List<String> loreList = main.getSettingsConfig().getStringList("OblivionShard.OblivionShard-Lore");
        for (String lore : loreList) {
            String withPAPISet1 = main.setPlaceholders(p, lore);
            enchantmentLore.add(Main.color(withPAPISet1)
                    .replace("{oblivionShardChance}", String.valueOf(main.getSettingsConfig().getDouble("OblivionShard.OblivionShard-ReturnEnchant-Chance") * 100)));
        }

        boolean addGlint = main.getSettingsConfig().getBoolean("OblivionShard.OblivionShard-Glint-Toggle");
        if(addGlint)
        {
            // Add Glint effect
            shardMeta.addEnchant(Enchantment.INFINITY, 1, true);
            shardMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        int shardID = random.nextInt();

        PersistentDataContainer pdc = shardMeta.getPersistentDataContainer();
        pdc.set(Main.customShardKeys, PersistentDataType.STRING, "Oblivion-Shard" + ":" + shardID);

        shardMeta.setLore(enchantmentLore);
        shard.setItemMeta(shardMeta);

        // Add items to player's inventory if player is not null
        if (p != null) {
            for (int i = 0; i < shardAmount; i++) {
                // Check if there's space in the player's inventory
                HashMap<Integer, ItemStack> remainingItems = p.getInventory().addItem(shard);

                // If the inventory is full and the item could not be added, drop it at the player's feet
                if (!remainingItems.isEmpty()) {
                    for (ItemStack item : remainingItems.values()) {
                        p.getWorld().dropItemNaturally(p.getLocation(), item);
                    }
                }
            }
        }

        return shard;
    }
}
