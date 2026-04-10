package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.List;

public class Regrowth implements Listener {

    private Main main;
    private WorldGuardPlugin worldGuardPlugin;

    public Regrowth (Main main) {
        this.main = main;
        this.worldGuardPlugin = main.getWorldGuardPlugin();
    }

    public boolean hasTool (Player p) {
        // Get the player's tool(s)
        ItemStack tool = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.REGROWTH.Enchantment-Apply-Item");

        // Check if the player has a tool in their hand
        return tool != null && armorMat.contains(tool.getType().toString());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if(e.isCancelled()) return;
        //Grabbing the player
        Player p = e.getPlayer();
        // Grabbing the broken block
        Block cropBroken = e.getBlock();

        //WorldGuard Checks
        if(worldGuardPlugin != null && worldGuardPlugin.isEnabled() && !p.isOp())
        {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(cropBroken.getWorld()));

            if (regions != null) {
                ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.asBlockVector(cropBroken.getLocation()));

                for (ProtectedRegion region : set) {
                    if (!region.getMembers().contains(worldGuardPlugin.wrapPlayer(p)) &&
                            !region.getOwners().contains(worldGuardPlugin.wrapPlayer(p))) {
                        // Cancel the event if the player is not a member or owner of the region
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // Check if the player has an enchanted tool
        if(hasTool(p)) {

            // Get Enchantment Enabled Status
            boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.REGROWTH.Enchantment-Enabled");
            // if Enchantment Enabled = false return.
            if(!isEnchantmentEnabled) return;

            for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
                if (enchant.name.contains("REGROWTH")) {
                    // PUT ENCHANT LOGIC HERE
                    Material cropBrokenMat = cropBroken.getType();
                    BlockData cropBrokenData = cropBroken.getBlockData();

                    ItemStack tool = p.getInventory().getItemInMainHand();
                    ItemMeta toolMeta = tool.getItemMeta();

                    // Check if the block is ageable (crop)
                    if (!(cropBrokenData instanceof Ageable)) {
                        return;
                    }

                    Location cropLoc = cropBroken.getLocation();

                    // Check if the block below is farmland or netherwart
                    if (cropLoc.clone().subtract(0, 1, 0).getBlock().getType() != Material.FARMLAND && cropLoc.clone().subtract(0, 1, 0).getBlock().getType() != Material.SOUL_SAND) {
                        return;
                    }

                    if (((Ageable) cropBrokenData).getAge() == ((Ageable) cropBrokenData).getMaximumAge()) {
                        // Collect the drops manually
                        Collection<ItemStack> drops = cropBroken.getDrops();

                        // Drop the items manually at the block's location
                        for (ItemStack drop : drops) {
                            cropLoc.getWorld().dropItemNaturally(cropLoc, drop);
                        }
                    }

                    // Determine the type of crop and replant it as the initial stage
                    Material seedType;
                    switch (cropBrokenMat) {
                        case WHEAT:
                            seedType = Material.WHEAT;
                            break;
                        case CARROTS:
                            seedType = Material.CARROT;
                            break;
                        case POTATOES:
                            seedType = Material.POTATO;
                            break;
                        case BEETROOTS:
                            seedType = Material.BEETROOTS;
                            break;
                        case NETHER_WART:
                            seedType = Material.NETHER_WART;
                            break;
                        default:
                            return;
                    }

                    // Plant the crop by setting the block type to the crop type (not the seed)
                    cropBroken.setType(cropBrokenMat);

                    // Set the crop's age to 0 (seedling state)
                    BlockData newCropData = cropLoc.getBlock().getBlockData();
                    if (newCropData instanceof Ageable) {
                        Ageable ageableCrop = (Ageable) newCropData;
                        ageableCrop.setAge(0);  // Set age to 0 (seedling state)
                        cropLoc.getBlock().setBlockData(ageableCrop);
                    }

                    // Force a block update to ensure the change is reflected
                    cropLoc.getBlock().getState().update(true, false);

                    if (toolMeta instanceof Damageable) {
                        Damageable damageableToolMeta = (Damageable) toolMeta;

                        damageableToolMeta.setDamage(damageableToolMeta.getDamage() + 1);

                        tool.setItemMeta(damageableToolMeta);
                    }

                    e.setCancelled(true);
                    // END ENCHANT LOGIC
                }
            }
        }
    }
}
