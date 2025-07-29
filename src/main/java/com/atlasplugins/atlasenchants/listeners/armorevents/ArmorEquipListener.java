package com.atlasplugins.atlasenchants.listeners.armorevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ArmorEquipListener implements Listener {

    private Main main;
    private final List<Material> blockedMaterials;

    public ArmorEquipListener(Main main, List<String> blockedMaterialNames) {
        this.main = main;
        this.blockedMaterials = blockedMaterialNames.stream()
                .map(Material::getMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

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
        ArmorEquipEvent.ArmorType armorType = null;

        // player.sendMessage(Main.color("&c----- &6&lInventoryClickEvent EVENT CALLED &c-----"));

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
//        if(action == InventoryAction.PICKUP_ALL || action == InventoryAction.PICKUP_ONE || action == InventoryAction.PICKUP_SOME || action == InventoryAction.PICKUP_HALF) {
//            if (event.getSlotType() == InventoryType.SlotType.ARMOR || (rawSlot >= 5 && rawSlot <= 8)) {
//                // player.sendMessage(Main.color("&3Pick Action"));
//                // Determine if the item being Unequipped is armor or not
//                if (isArmor(clickedItem)) {
//                    // Unequipped armor to another slot
//                    equippedArmor = cursorItem != null && cursorItem.getType() != Material.AIR ? cursorItem : null;
//                    unequippedArmor = clickedItem;
//                }
//            }
//        }
        if ((action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD)) {
            if (event.getSlotType() == InventoryType.SlotType.ARMOR || (rawSlot >= 5 && rawSlot <= 8)) {
                if (isArmor(clickedItem)) {
                    unequippedArmor = clickedItem;
                    equippedArmor = player.getInventory().getItem(event.getHotbarButton());
                }
            }
        }


        // Detecting Adding Armor from the armor slots
        if(action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE || action == InventoryAction.PLACE_SOME){
            if (event.getSlotType() == InventoryType.SlotType.ARMOR || (rawSlot >= 5 && rawSlot <= 8)) {
                // player.sendMessage(Main.color("&3Place Action"));
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
            // player.sendMessage(Main.color("&3Shift Click Action"));
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
            // player.sendMessage(Main.color("&3Shift Click Action"));
            // Determine if armor is being equipped or unequipped
            if(isArmor(clickedItem)) {
                // Unequipped armor to another slot
                equippedArmor = cursorItem != null && cursorItem.getType() != Material.AIR ? cursorItem : null;
                unequippedArmor = clickedItem;
            }
        }

//        if(clickedItem != null) {
//            System.out.println("clickedItem: " + clickedItem.getType().toString());
//        }
//        if (cursorItem != null) {
//            System.out.println("cursorItem: " + cursorItem.getType().toString());
//        }
//        System.out.println("-------------------------------");

//         player.sendMessage(Main.color("&3Armor Type: &f" + armorType.toString()));
//        if(clickedItem != null) {
//             player.sendMessage(Main.color("&bEquippedArmor: &f" + (equippedArmor != null ? equippedArmor.getType().toString() : "null")));
//        }
//        if (cursorItem != null)
//        {
//             player.sendMessage(Main.color("&bUnequippedArmor: &f" + (unequippedArmor != null ? unequippedArmor.getType().toString() : "null")));
//        }

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
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack cursorItem = event.getOldCursor();

        if (!isArmor(cursorItem)) return;

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= 5 && rawSlot <= 8) {
                ArmorEquipEvent.ArmorType armorType = getArmorType(rawSlot);
                if (armorType == null) continue;

                ItemStack unequippedArmor = event.getView().getItem(rawSlot);
                ItemStack equippedArmor = cursorItem;

                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, unequippedArmor, equippedArmor, armorType, ArmorEquipEvent.EquipMethod.DRAG);
                Bukkit.getScheduler().runTaskLater(main, () -> player.getServer().getPluginManager().callEvent(armorEquipEvent), 1);

                if (armorEquipEvent.isCancelled()) {
                    event.setCancelled(true);
                }
            }
        }
    }

//    @EventHandler
//    public void onInventoryDrag(InventoryDragEvent event) {
//        if (!(event.getWhoClicked() instanceof Player)) {
//            return;
//        }
//
//        Player player = (Player) event.getWhoClicked();
//        player.sendMessage(Main.color("&c----- &6&lInventoryDragEvent EVENT CALLED &c-----"));
//        ItemStack cursorItem = event.getCursor();
//
//        for (int slot : event.getRawSlots()) {
//            if (slot >= 5 && slot <= 8) {
//                ItemStack equippedArmor = cursorItem;
//                ItemStack unequippedArmor = event.getView().getItem(slot);
//
//                ArmorEquipEvent.ArmorType armorType = getArmorType(slot);
//                ArmorEquipEvent armorEquipEvent = new ArmorEquipEvent(player, unequippedArmor, equippedArmor, armorType, ArmorEquipEvent.EquipMethod.DRAG);
//
//                player.getServer().getPluginManager().callEvent(armorEquipEvent);
//
//                if (armorEquipEvent.isCancelled()) {
//                    event.setCancelled(true);
//                }
//            }
//        }
//    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if the hand used is not the main hand
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // Check if the action is physical
        if(event.getAction() == Action.PHYSICAL) return;

        // Check if the action is a left click (optional)
        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        // Get the item in the main hand
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is a type of armor, if not, return
        if (!isArmor(item)) {
            return;
        }

        if(event.getClickedBlock() != null && event.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking()){
            Material mat = event.getClickedBlock().getType();
            if(blockedMaterials.contains(mat)) return;
        }

        // player.sendMessage(Main.color("&c----- &6&lPlayerInteractEvent EVENT CALLED &c-----"));

        // Get the armor type
        ArmorEquipEvent.ArmorType armorType = getArmorType(item.getType());

        // Determine the old armor item based on the armor type
        ItemStack oldArmor = null;
        switch (armorType) {
            case HELMET:
                oldArmor = player.getInventory().getHelmet();
                break;
            case CHESTPLATE:
                oldArmor = player.getInventory().getChestplate();
                break;
            case LEGGINGS:
                oldArmor = player.getInventory().getLeggings();
                break;
            case BOOTS:
                oldArmor = player.getInventory().getBoots();
                break;
            default:
                break;
        }

        ArmorEquipEvent armorEquipEvent;

        if (oldArmor == null || !isArmor(oldArmor)) {
            // You do not have any armor in the specific slot.
            // player.sendMessage(Main.color("&cYou do not have any armor in the " + armorType.toString().toLowerCase() + " slot."));
            armorEquipEvent = new ArmorEquipEvent(player, null, item, armorType, ArmorEquipEvent.EquipMethod.HOTBAR);
        } else {
            // You have armor equipped in the specific slot!
            // player.sendMessage(Main.color("&aYou have armor equipped in the " + armorType.toString().toLowerCase() + " slot!"));
            armorEquipEvent = new ArmorEquipEvent(player, oldArmor, item, armorType, ArmorEquipEvent.EquipMethod.HOTBAR);
        }

        // Schedule a delayed task to handle messaging and effects after inventory update
        Bukkit.getScheduler().runTaskLater(main, () -> {
            player.getServer().getPluginManager().callEvent(armorEquipEvent);
        }, 1); // Delay of 1 tick (0.05 seconds) to allow inventory to update

        // Check if the event was cancelled
        if (armorEquipEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onBlockDispenseArmor(BlockDispenseArmorEvent event) {
        if (event.getTargetEntity() instanceof Player) {
            Player player = (Player) event.getTargetEntity();
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
            case ELYTRA: // Add this to support Elytras as chestplates
                return ArmorEquipEvent.ArmorType.ELYTRA;
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