package com.atlasplugins.atlasenchants.listeners.armorevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Warning;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;


public class ArmorEquipListener implements Listener {

    private Main main;
    public ArmorEquipListener(Main main) {this.main = main;}

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        int rawSlot = event.getRawSlot();
        InventoryAction action = event.getAction();

        // Logging for debugging purposes
        Bukkit.getServer().getLogger().info("Action: " + action + ", RawSlot: " + rawSlot + ", ClickedItem: " + clickedItem + ", CursorItem: " + cursorItem);

        // Detecting shift-click to equip or unequip armor
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && isArmor(clickedItem)) {
            ArmorEquipEvent.ArmorType armorType = getArmorType(clickedItem.getType());
            ItemStack unequippedArmor = null;
            ItemStack equippedArmor = null;

            // Determine if armor is being equipped or unequipped
            if (event.getSlotType() == InventoryType.SlotType.ARMOR || (rawSlot >= 36 && rawSlot <= 39)) {
                // Equipping armor
                equippedArmor = clickedItem;
                unequippedArmor = cursorItem;
            } else {
                // Unequipping armor
                equippedArmor = cursorItem;
                unequippedArmor = clickedItem;
            }

            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, unequippedArmor, equippedArmor, armorType, ArmorEquipEvent.EquipMethod.SHIFT_CLICK);

            // Schedule a delayed task to handle messaging and effects after inventory update
            Bukkit.getScheduler().runTaskLater(main, () -> {
                player.getServer().getPluginManager().callEvent(armorEquipEvent);
            }, 1); // Delay of 1 tick (0.05 seconds) to allow inventory to update

            if (armorEquipEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }
        }
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        for (int slot : event.getRawSlots()) {
            if (slot >= 5 && slot <= 8) {
                ItemStack equippedArmor = event.getCursor();
                ArmorEquipEvent.ArmorType armorType = getArmorType(slot);
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, null, equippedArmor, armorType, ArmorEquipEvent.EquipMethod.DRAG);

                player.getServer().getPluginManager().callEvent(armorEquipEvent);

                if (armorEquipEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (isArmor(item)) {
            ArmorEquipEvent.ArmorType armorType = getArmorType(item.getType());
            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, null, item, armorType, ArmorEquipEvent.EquipMethod.HOTBAR);

            player.getServer().getPluginManager().callEvent(armorEquipEvent);

            if (armorEquipEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockDispenseArmor(BlockDispenseArmorEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            Player player = (Player) event.getTargetEntity();
            ItemStack equippedArmor = event.getItem();
            ArmorEquipEvent.ArmorType armorType = getArmorType(equippedArmor.getType());
            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, null, equippedArmor, armorType, ArmorEquipEvent.EquipMethod.DISPENSER);

            player.getServer().getPluginManager().callEvent(armorEquipEvent);

            if (armorEquipEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack brokenArmor = event.getBrokenItem();
        ArmorEquipEvent.ArmorType armorType = getArmorType(brokenArmor.getType());
        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, brokenArmor, null, armorType, ArmorEquipEvent.EquipMethod.BROKE);

        player.getServer().getPluginManager().callEvent(armorEquipEvent);

        if (armorEquipEvent.isCancelled()) {
            // Prevent the item from breaking by removing it from the player's inventory
            PlayerInventory inventory = player.getInventory();
            if (inventory.contains(brokenArmor)) {
                inventory.removeItem(brokenArmor);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null) {
                ArmorEquipEvent.ArmorType armorType = getArmorType(armor.getType());
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, armor, null, armorType, ArmorEquipEvent.EquipMethod.DEATH);

                player.getServer().getPluginManager().callEvent(armorEquipEvent);

                if (armorEquipEvent.isCancelled()) {
                    event.getDrops().remove(armor);
                }
            }
        }
    }

    private boolean isArmor(ItemStack item) {
        return getArmorType(item.getType()) != null;
    }

    private ArmorEquipEvent.ArmorType getArmorType(Material material) {
        switch (material) {
            case LEATHER_HELMET:
            case CHAINMAIL_HELMET:
            case IRON_HELMET:
            case TURTLE_HELMET:
            case GOLDEN_HELMET:
            case DIAMOND_HELMET:
            case NETHERITE_HELMET: // Added Netherite Helmet
                return ArmorEquipEvent.ArmorType.HELMET;
            case LEATHER_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case IRON_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case NETHERITE_CHESTPLATE: // Added Netherite Chestplate
                return ArmorEquipEvent.ArmorType.CHESTPLATE;
            case LEATHER_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case IRON_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case NETHERITE_LEGGINGS: // Added Netherite Leggings
                return ArmorEquipEvent.ArmorType.LEGGINGS;
            case LEATHER_BOOTS:
            case CHAINMAIL_BOOTS:
            case IRON_BOOTS:
            case GOLDEN_BOOTS:
            case DIAMOND_BOOTS:
            case NETHERITE_BOOTS: // Added Netherite Boots
                return ArmorEquipEvent.ArmorType.BOOTS;
            default:
                System.out.println("Unknown material: {0}" + material);
                return null;
        }
    }

    private ArmorEquipEvent.ArmorType getArmorType(int slot) {
        switch (slot) {
            case 5:
                return ArmorEquipEvent.ArmorType.HELMET;
            case 6:
                return ArmorEquipEvent.ArmorType.CHESTPLATE;
            case 7:
                return ArmorEquipEvent.ArmorType.LEGGINGS;
            case 8:
                return ArmorEquipEvent.ArmorType.BOOTS;
            default:
                System.out.println("Unknown slot: {0}" + slot);
                return null;
        }
    }

}