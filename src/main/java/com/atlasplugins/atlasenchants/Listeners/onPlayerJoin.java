package com.atlasplugins.atlasenchants.Listeners;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class onPlayerJoin implements Listener {

    private Main main;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        p.sendMessage(main.color("&cW&ee&bl&dc&4o&am&2e " + p.getName()));
    }

}
