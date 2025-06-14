package com.atlasplugins.atlasenchants.guis;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public abstract class Gui {

    protected Inventory inventory;
    protected Player player;

    public Gui(Player player, String title, int size) {
        this.player = player;
        this.inventory = GuiManager.createGui(player, title, size);
    }

    public abstract void setupItems();

    public abstract void handleClick(InventoryClickEvent event);

    public void open() {
        setupItems(); // Setup items when opening
        GuiManager.setOpenGui(player, this); // Track it
        GuiManager.openGui(player, inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public abstract void onInventoryClose(InventoryCloseEvent event);
}
