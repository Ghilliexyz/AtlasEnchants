package com.atlasplugins.atlasenchants.listeners.armorevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an event triggered when a player equips or unequips armor.
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
     * Constructs a new ArmorEquipEvent.
     *
     * @param player           The player involved in the event.
     * @param unequippedArmor  The armor item that was unequipped.
     * @param equippedArmor    The armor item that was equipped.
     * @param armorType        The type of armor being affected (helmet, chestplate, etc.).
     * @param equipMethod      The method used to equip the armor (shift-click, drag, etc.).
     */
    public ArmorEquipEvent(Player player, ItemStack unequippedArmor, ItemStack equippedArmor, ArmorType armorType, EquipMethod equipMethod) {
        this.player = player;
        this.unequippedArmor = unequippedArmor;
        this.equippedArmor = equippedArmor;
        this.armorType = armorType;
        this.equipMethod = equipMethod;
        this.cancelled = false;
    }

    /**
     * Retrieves the player involved in the armor event.
     *
     * @return The player involved in the event.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Retrieves the armor item that was unequipped.
     *
     * @return The ItemStack representing the unequipped armor item.
     */
    public ItemStack getUnequippedArmor() {
        return unequippedArmor;
    }

    /**
     * Retrieves the armor item that was equipped.
     *
     * @return The ItemStack representing the equipped armor item.
     */
    public ItemStack getEquippedArmor() {
        return equippedArmor;
    }

    /**
     * Retrieves the type of armor being affected.
     *
     * @return The ArmorType representing the type of armor (helmet, chestplate, etc.).
     */
    public ArmorType getArmorType() {
        return armorType;
    }

    /**
     * Retrieves the method used to equip the armor.
     *
     * @return The EquipMethod representing the method used to equip the armor.
     */
    public EquipMethod getEquipMethod() {
        return equipMethod;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Retrieves the HandlerList for ArmorEquipEvent.
     *
     * @return The HandlerList associated with ArmorEquipEvent.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Represents the types of armor that can be affected by the event.
     */
    public enum ArmorType {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS
    }

    /**
     * Represents the methods used to equip armor.
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
