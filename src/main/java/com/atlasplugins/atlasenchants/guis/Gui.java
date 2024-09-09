package com.atlasplugins.atlasenchants.guis;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class Gui {

    protected Inventory inventory;
    protected Player player;

    public Gui(Player player, String title, int size) {
        this.player = player;
        this.inventory = GuiManager.createGui(player, title, size);
    }

    public abstract void setupItems(); // Method to be implemented by subclasses

    public void open() {
        GuiManager.openGui(player, inventory);
    }
}

