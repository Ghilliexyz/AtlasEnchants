package com.atlasplugins.atlasenchants.commands.users;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.commands.AbstractCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class TestCommand extends AbstractCommand {

    private final Main main;
    public TestCommand(Main main) {this.main = main;}

    @Override
    public void execute(JavaPlugin plugin, CommandSender sender, String label, List<String> args) {

        Player player = (Player) sender;

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if(itemInHand.getType().equals(Material.AIR)) return;

        ItemMeta itemMeta = itemInHand.getItemMeta();

        PersistentDataContainer itemPDC = itemMeta.getPersistentDataContainer();


        if (itemPDC.has(Main.customEnchantKeys, PersistentDataType.STRING)) {
            String existingEnchantData = itemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

            main.getLogger().info("|-------------------====1====-------------------|");
            main.getLogger().info("existingEnchantData: " + existingEnchantData);
        }else {
            main.getLogger().info("No Data for enchants");
        }

        if (itemPDC.has(Main.customShardKeys, PersistentDataType.STRING)) {
            String existingShardData = itemPDC.get(Main.customShardKeys, PersistentDataType.STRING);

            main.getLogger().info("|--------------------------------------|");
            main.getLogger().info("existingShardData: " + existingShardData);
        }else {
            main.getLogger().info("No Data for Shards");
        }

    }

    @Override
    public void complete(JavaPlugin plugin, CommandSender sender, String label, List<String> args, List<String> completions) {

    }

    @Override
    public List<String> getLabels() {
        return Collections.singletonList("test");
    }

    @Override
    public String getPermission() {
        return "atlasenchants.test";  // permission required for help command
    }
}

