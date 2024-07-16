package com.atlasplugins.atlasenchants.listeners.armorevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
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
        player.sendMessage(Main.color("&c----- &6&lInventoryClickEvent EVENT CALLED &c-----"));
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();
        int rawSlot = event.getRawSlot();
        InventoryAction action = event.getAction();

        ArmorEquipEvent.ArmorType armorType = null;

        // Determine armor type from clickedItem or cursorItem
        if (isArmor(clickedItem)) {
            armorType = getArmorType(clickedItem.getType());
        } else if (isArmor(cursorItem)) {
            armorType = getArmorType(cursorItem.getType());
        }

        // If armorType is still null, it means no valid armor was involved in the action
        if (armorType == null) {
            return;
        }

        ItemStack unequippedArmor = null;
        ItemStack equippedArmor = null;

        // Detecting Removing Armor from the armor slots
        if(action == InventoryAction.PICKUP_ALL || action == InventoryAction.PICKUP_ONE || action == InventoryAction.PICKUP_SOME || action == InventoryAction.PICKUP_HALF) {
            if (event.getSlotType() == InventoryType.SlotType.ARMOR || (rawSlot >= 5 && rawSlot <= 8)) {
                player.sendMessage(Main.color("&3Pick Action"));
                // Determine if the item being Unequipped is armor or not
                if (isArmor(clickedItem)) {
                    // Unequipped armor to another slot
                    equippedArmor = cursorItem != null && cursorItem.getType() != Material.AIR ? cursorItem : null;
                    unequippedArmor = clickedItem;
                }
            }
        }

        // Detecting Adding Armor from the armor slots
        if(action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE || action == InventoryAction.PLACE_SOME){
            if (event.getSlotType() == InventoryType.SlotType.ARMOR || (rawSlot >= 5 && rawSlot <= 8)) {
                player.sendMessage(Main.color("&3Place Action"));
                // Determine if the item being Equipped is armor or not
                if (isArmor(cursorItem)) {
                    // Equipping armor to another slot
                    equippedArmor = cursorItem;
                    unequippedArmor = clickedItem != null && clickedItem.getType() != Material.AIR ? clickedItem : null;
                }
            }
        }

        // Detecting shift-click Adding armor
        if ((action == InventoryAction.MOVE_TO_OTHER_INVENTORY)
                || (action == InventoryAction.HOTBAR_SWAP
                || action == InventoryAction.HOTBAR_MOVE_AND_READD)) {
            player.sendMessage(Main.color("&3Shift Click Action"));
            // Determine if armor is being equipped or unequipped
            if(isArmor(clickedItem)) {
                // Equipping armor to another slot
                equippedArmor = clickedItem;
                unequippedArmor = cursorItem != null && cursorItem.getType() != Material.AIR ? cursorItem : null;
            }
        }

        // Detecting shift-click Removing armor
        if ((action == InventoryAction.MOVE_TO_OTHER_INVENTORY && (rawSlot >= 5 && rawSlot <= 8))
                || (action == InventoryAction.HOTBAR_SWAP
                || action == InventoryAction.HOTBAR_MOVE_AND_READD)) {
            player.sendMessage(Main.color("&3Shift Click Action"));
            // Determine if armor is being equipped or unequipped
            if(isArmor(clickedItem)) {
                // Unequipped armor to another slot
                equippedArmor = cursorItem != null && cursorItem.getType() != Material.AIR ? cursorItem : null;
                unequippedArmor = clickedItem;
            }
        }

        System.out.println("clickedItem: " + clickedItem.getType().toString());
        System.out.println("cursorItem: " + cursorItem.getType().toString());
        System.out.println("-------------------------------");

        player.sendMessage(Main.color("&3Armor Type: &f" + armorType.toString()));
        player.sendMessage(Main.color("&bEquippedArmor: &f" + (equippedArmor != null ? equippedArmor.getType().toString() : "null")));
        player.sendMessage(Main.color("&bUnequippedArmor: &f" + (unequippedArmor != null ? unequippedArmor.getType().toString() : "null")));

        ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, unequippedArmor, equippedArmor, armorType, ArmorEquipEvent.EquipMethod.SHIFT_CLICK);

        if(unequippedArmor == null || unequippedArmor.getType().equals(Material.AIR))
        {
            // Schedule a delayed task to handle messaging and effects after inventory update
            Bukkit.getScheduler().runTaskLater(main, () -> {
                player.getServer().getPluginManager().callEvent(armorEquipEvent);
            }, 1); // Delay of 1 tick (0.05 seconds) to allow inventory to update
        }else {
            player.getServer().getPluginManager().callEvent(armorEquipEvent);
        }

        if (armorEquipEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }



    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack cursorItem = event.getCursor();

        for (int slot : event.getRawSlots()) {
            if (slot >= 5 && slot <= 8) {
                ItemStack equippedArmor = cursorItem;
                ItemStack unequippedArmor = event.getView().getItem(slot);

                ArmorEquipEvent.ArmorType armorType = getArmorType(slot);
                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, unequippedArmor, equippedArmor, armorType, ArmorEquipEvent.EquipMethod.DRAG);

                player.getServer().getPluginManager().callEvent(armorEquipEvent);

                if (armorEquipEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // broken
//    @EventHandler
//    public void onPlayerInteract(PlayerInteractEvent event) {
//        Player player = event.getPlayer();
//        player.sendMessage(Main.color("&c----- &6&lPlayerInteractEvent EVENT CALLED &c-----"));
//        ItemStack item = player.getInventory().getItemInMainHand();
//        if (isArmor(item)) {
//            ArmorEquipEvent.ArmorType armorType = getArmorType(item.getType());
//            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, null, item, armorType, ArmorEquipEvent.EquipMethod.HOTBAR);
//
//            player.getServer().getPluginManager().callEvent(armorEquipEvent);
//
//            if (armorEquipEvent.isCancelled()) {
//                event.setCancelled(true);
//            }
//        }
//    }

    @EventHandler
    public void onBlockDispenseArmor(BlockDispenseArmorEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            Player player = (Player) event.getTargetEntity();
            player.sendMessage(Main.color("&c----- &6&lBlockDispenseArmorEvent EVENT CALLED &c-----"));
            ItemStack equippedArmor = event.getItem();

            ArmorEquipEvent.ArmorType armorType = getArmorType(equippedArmor.getType());
            ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, null, equippedArmor, armorType, ArmorEquipEvent.EquipMethod.DISPENSER);

            if(isArmor(equippedArmor))
            {
                // Schedule a delayed task to handle messaging and effects after inventory update
                Bukkit.getScheduler().runTaskLater(main, () -> {
                    player.getServer().getPluginManager().callEvent(armorEquipEvent);
                }, 1); // Delay of 1 tick (0.05 seconds) to allow inventory to update
            }

            if (armorEquipEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        player.sendMessage(Main.color("&c----- &6&lPlayerItemBreakEvent EVENT CALLED &c-----"));
        ItemStack brokenArmor = event.getBrokenItem();
        if(isArmor(brokenArmor)) {
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
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.sendMessage(Main.color("&c----- &6&lPlayerDeathEvent EVENT CALLED &c-----"));
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
        return item != null && item.getType() != Material.AIR && getArmorType(item.getType()) != null;
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
                return null;
        }
    }


}