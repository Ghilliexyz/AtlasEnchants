package com.atlasplugins.atlasenchants.listeners.armorevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Event that is called when a player equips or unequips armor.
 * This event can be cancelled to prevent the armor change.
 */
public class ArmorEquipEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack unequippedArmor;
    private final ItemStack equippedArmor;
    private final ArmorType armorType;
    private final EquipMethod equipMethod;
    private boolean cancelled;

    /**
     * Constructor for the ArmorEquipEvent.
     * @param player The player who is equipping or unequipping armor.
     * @param unequippedArmor The ItemStack of the armor being removed.
     * @param equippedArmor The ItemStack of the armor being equipped.
     * @param armorType The type of armor being changed.
     * @param equipMethod The method used to equip or unequip the armor.
     */
    public ArmorEquipEvent(Player player, ItemStack unequippedArmor, ItemStack equippedArmor, ArmorType armorType, EquipMethod equipMethod) {
        this.player = player;
        this.unequippedArmor = unequippedArmor != null ? unequippedArmor.clone() : null;
        this.equippedArmor = equippedArmor != null ? equippedArmor.clone() : null;
        this.armorType = armorType;
        this.equipMethod = equipMethod;
        this.cancelled = false;

        player.sendMessage(Main.color("&c----- &6&lArmorEquipEvent Constructor &c-----"));
        player.sendMessage(Main.color("&ePlayer: " + this.player.getName()));
        player.sendMessage(Main.color("&eUnequipped Armor: " + (this.unequippedArmor != null ? this.unequippedArmor.getType() : "null")));
        player.sendMessage(Main.color("&eEquipped Armor: " + (this.equippedArmor != null ? this.equippedArmor.getType() : "null")));
        player.sendMessage(Main.color("&eArmor Type: " + this.armorType));
        player.sendMessage(Main.color("&eEquip Method: " + this.equipMethod));
        player.sendMessage(Main.color("&eCancelled: " + this.cancelled));
    }

    /**
     * Gets the player involved in this event.
     * @return The player who is equipping or unequipping armor.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the armor that was unequipped.
     * @return The ItemStack of the unequipped armor, or null if no armor was unequipped.
     */
    public ItemStack getUnequippedArmor() {
        return unequippedArmor != null ? unequippedArmor.clone() : null;
    }

    /**
     * Gets the armor that was equipped.
     * @return The ItemStack of the equipped armor, or null if no armor was equipped.
     */
    public ItemStack getEquippedArmor() {
        return equippedArmor != null ? equippedArmor.clone() : null;
    }

    /**
     * Gets the type of armor involved in this event.
     * @return The type of armor (HELMET, CHESTPLATE, LEGGINGS, BOOTS).
     */
    public ArmorType getArmorType() {
        return armorType;
    }

    /**
     * Gets the method used to equip or unequip the armor.
     * @return The method of equipping or unequipping (SHIFT_CLICK, DRAG, PICK_DROP, etc.).
     */
    public EquipMethod getEquipMethod() {
        return equipMethod;
    }

    /**
     * Gets the HandlerList for this event.
     * This is required by Bukkit's event system.
     * @return The static HandlerList for this event.
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the static HandlerList for this event.
     * This is required by Bukkit's event system.
     * @return The static HandlerList for this event.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Checks if this event has been cancelled.
     * @return True if the event is cancelled, false otherwise.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancelled status of this event.
     * If an event is cancelled, the armor change will not occur.
     * @param cancel True to cancel the event, false to allow it.
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Enum representing the type of armor involved in the event.
     */
    public enum ArmorType {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS
    }

    /**
     * Enum representing the method used to equip or unequip the armor.
     */
    public enum EquipMethod {
        SHIFT_CLICK,
        DRAG,
        PICK_DROP,
        HOTBAR,
        HOTBAR_SWAP,
        DISPENSER,
        BROKE,
        DEATH
    }
}
