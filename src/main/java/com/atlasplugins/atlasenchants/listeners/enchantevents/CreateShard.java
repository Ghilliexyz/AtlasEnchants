package com.atlasplugins.atlasenchants.listeners.enchantevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CreateShard implements Listener {

    private Main main;

    public CreateShard(Main main) {
        this.main = main;
    }

    public ItemStack CreateShardItem(int shardAmount, Player p) {
        ItemStack shard = new ItemStack(Material.valueOf(main.getSettingsConfig().getString("OblivionShard.OblivionShard-Item")));
        ItemMeta shardMeta = shard.getItemMeta();

        String displayName = main.getEnchantmentsConfig().getString("OblivionShard.OblivionShard-DisplayName");
        String withPAPISet = main.setPlaceholders(p, displayName);
        shardMeta.setDisplayName(Main.color(withPAPISet));

        ArrayList<String> enchantmentLore = new ArrayList<>();
        List<String> loreList = main.getEnchantmentsConfig().getStringList("OblivionShard.OblivionShard-Lore");
        for (String lore : loreList) {
            String withPAPISet1 = main.setPlaceholders(p, lore);
            enchantmentLore.add(Main.color(withPAPISet1));
        }

        PersistentDataContainer pdc = shardMeta.getPersistentDataContainer();
        pdc.set(Main.customShardKeys, PersistentDataType.STRING, "Oblivion-Shard" + ":" + shardAmount);

        shardMeta.setLore(enchantmentLore);
        shard.setItemMeta(shardMeta);

        // Add items to player's inventory if player is not null
        if (p != null) {
            for (int i = 0; i < shardAmount; i++) {
                p.getInventory().addItem(shard);
            }
        }

        return shard;
    }
}
