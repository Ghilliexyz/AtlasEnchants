package com.atlasplugins.atlasenchants.listeners.armorevents;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ArmorEquipEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final ItemStack unequippedArmor;
    private final ItemStack equippedArmor;
    private final ArmorType armorType;
    private final EquipMethod equipMethod;
    private boolean cancelled;

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

    public Player getPlayer() {
        return player;
    }

    public ItemStack getUnequippedArmor() {
        return unequippedArmor != null ? unequippedArmor.clone() : null;
    }

    public ItemStack getEquippedArmor() {
        return equippedArmor != null ? equippedArmor.clone() : null;
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    public EquipMethod getEquipMethod() {
        return equipMethod;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

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

    public enum ArmorType {
        HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS
    }

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
