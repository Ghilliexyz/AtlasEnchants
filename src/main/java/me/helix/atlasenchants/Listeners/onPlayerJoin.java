package me.helix.atlasenchants.Listeners;

import me.helix.atlasenchants.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.xezard.glow.data.glow.Glow;

public class onPlayerJoin implements Listener {

    private Main main;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        p.sendMessage(main.color("&cW&ee&bl&dc&4o&am&2e " + p.getName()));
    }

}
