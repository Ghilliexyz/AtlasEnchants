package com.atlasplugins.atlasenchants.Enchants;

import com.atlasplugins.atlasenchants.Main;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Fearsight implements Listener {

    private Main main;
    public Fearsight (Main main) {
        this.main = main;
    }

    private List<Entity> ListEntity;

    public boolean hasHelmet (Player p)
    {
        // Get the players inventory
        ItemStack helmet = p.getInventory().getHelmet();

        // Get the list of items the Enchant can be applied to.
        List<String> helmetMat = main.getConfig().getStringList("Enchantments.FEARSIGHT.Enchantment-Apply-Item");

        return helmet != null && helmetMat.contains(helmet.getType().toString());
    }


    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if(hasHelmet(p))
        {
            PersistentDataContainer enchantedItemPDC = p.getInventory().getHelmet().getItemMeta().getPersistentDataContainer();
            String enchantedItemData = enchantedItemPDC.get(Main.customEnchantKeys, PersistentDataType.STRING);

            String[] enchantParts = enchantedItemData.split(":");
            String enchantName = enchantParts[0];
            int enchantLevel = Integer.parseInt(enchantParts[1]);

            ListEntity = p.getNearbyEntities(main.getConfig().getInt("Enchantments.FEARSIGHT.Radius-of-glowing-" + enchantLevel), main.getConfig().getInt("Enchantments.FEARSIGHT.Radius-of-glowing-" + enchantLevel), main.getConfig().getInt("Enchantments.FEARSIGHT.Radius-of-glowing-" + enchantLevel));

            String hostileMobColor = main.getConfig().getString("Enchantments.FEARSIGHT.Hostile-GLOW-Colour").toUpperCase();
            String passiveMobColor = main.getConfig().getString("Enchantments.FEARSIGHT.Passive-GLOW-Colour").toUpperCase();
            String normalMobColor = main.getConfig().getString("Enchantments.FEARSIGHT.Player-Villager").toUpperCase();

            if (enchantName.contains("FEARSIGHT")) {
                for (Entity entity : ListEntity) {
                    if (entity instanceof Monster ||
                            entity instanceof Flying ||
                            entity instanceof Slime ||
                            entity instanceof Boss) {
                        try {
                            main.glowingEntities.setGlowing(entity, p, ChatColor.valueOf(hostileMobColor));
                        } catch (ReflectiveOperationException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else if (entity instanceof Animals ||
                            entity instanceof Ambient ||
                            entity instanceof WaterMob) {

                        try {
                            main.glowingEntities.setGlowing(entity, p, ChatColor.valueOf(passiveMobColor));
                        } catch (ReflectiveOperationException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else if (entity instanceof Player ||
                            entity instanceof Villager ||
                            entity instanceof WanderingTrader ||
                            entity instanceof IronGolem) {
                        try {
                            main.glowingEntities.setGlowing(entity, p, ChatColor.valueOf(normalMobColor));
                        } catch (ReflectiveOperationException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                    if (entity.getLocation().distance(p.getLocation()) >= main.getConfig().getInt("Enchantments.FEARSIGHT.Radius-of-glowing-" + enchantLevel)) {
                        try {
                            main.glowingEntities.unsetGlowing(entity, p);
                        } catch (ReflectiveOperationException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
        }
        else
        {
            if(ListEntity == null){return;}
            try {
                for (Entity removeGlow : ListEntity) {
                    main.glowingEntities.unsetGlowing(removeGlow, p);
                }
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @EventHandler
    public void PlayerDeathEvent(PlayerDeathEvent e)
    {
        Player player = e.getEntity();
        if(ListEntity == null) {return;}
        for (Entity entity : ListEntity) {
            try {
                main.glowingEntities.unsetGlowing(entity, player);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @EventHandler
    public void PlayerLeaveEvent(PlayerQuitEvent e)
    {
        Player player = e.getPlayer();
        if(ListEntity == null) {return;}
        for (Entity entity : ListEntity) {
            try {
                main.glowingEntities.unsetGlowing(entity, player);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
