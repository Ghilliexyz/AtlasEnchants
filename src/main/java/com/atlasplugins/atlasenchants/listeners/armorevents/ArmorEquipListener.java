package com.atlasplugins.atlasenchants.listeners.armorevents;

import org.bukkit.Material;
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

//        main.getLogger().log(Level.INFO, "Action: {0}, Slot: {1}, ClickedItem: {2}, CursorItem: {3}",
//                new Object[]{action, rawSlot, clickedItem, cursorItem});

        // Detecting shift-click to equip or unequip armor
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            if (isArmor(clickedItem)) {
                ArmorEquipEvent.ArmorType armorType = getArmorType(clickedItem.getType());
                if (armorType == null) {
//                    logger.log(Level.WARNING, "ArmorType is null for material: {0}", clickedItem.getType());
                    return;  // Prevent null ArmorType
                }

                ItemStack unequippedArmor = null; // No armor unequipped during this action
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, unequippedArmor, clickedItem, armorType, ArmorEquipEvent.EquipMethod.SHIFT_CLICK);
                player.getServer().getPluginManager().callEvent(armorEquipEvent);

                if (armorEquipEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }

        // Detecting click to equip or unequip armor directly
        if (event.getSlotType() == InventoryType.SlotType.ARMOR || (rawSlot >= 36 && rawSlot <= 39)) {
            ArmorEquipEvent.ArmorType armorType = getArmorType(rawSlot);
            if (armorType == null) {
//                logger.log(Level.WARNING, "ArmorType is null for slot: {0}", rawSlot);
                return;  // Prevent null ArmorType
            }

            ArmorEquipEvent.EquipMethod equipMethod = (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD) ?
                    ArmorEquipEvent.EquipMethod.HOTBAR_SWAP : ArmorEquipEvent.EquipMethod.PICK_DROP;

            ItemStack equippedArmor = (clickedItem != null) ? clickedItem : null;
            ItemStack unequippedArmor = (clickedItem == null && cursorItem != null) ? cursorItem : null;

            if (equippedArmor == null && unequippedArmor == null) {
                return; // No valid action detected
            }

            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, unequippedArmor, equippedArmor, armorType, equipMethod);
            player.getServer().getPluginManager().callEvent(armorEquipEvent);

            if (armorEquipEvent.isCancelled()) {
                event.setCancelled(true);
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
            if (slot >= 36 && slot <= 39) {
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
//                logger.log(Level.WARNING, "Unknown material: {0}", material);
                return null;
        }
    }

    private ArmorEquipEvent.ArmorType getArmorType(int slot) {
        switch (slot) {
            case 39:
                return ArmorEquipEvent.ArmorType.HELMET;
            case 38:
                return ArmorEquipEvent.ArmorType.CHESTPLATE;
            case 37:
                return ArmorEquipEvent.ArmorType.LEGGINGS;
            case 36:
                return ArmorEquipEvent.ArmorType.BOOTS;
            default:
//                logger.log(Level.WARNING, "Unknown slot: {0}", slot);
                return null;
        }
    }
}
