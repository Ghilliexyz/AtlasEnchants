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

    // For GUIs backed by a non-chest menu (e.g. the anvil used by Circe's Brand), which build
    // their view through MenuType rather than GuiManager.createGui. Subclasses using this must
    // also override open(), since the view - not the Inventory - is what gets opened.
    protected Gui(Player player, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
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
