package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.blockevents.ChainBlockBreakEvent;
import com.atlasplugins.atlasenchants.listeners.enchantevents.CreateAltarOfCirce;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SafeMiner implements Listener {

    private Main main;
    private WorldGuardPlugin worldGuardPlugin;

    // Block locations broken by a SafeMiner tool this tick, mapped to the miner. Filled in
    // onBlockBreak and consumed by onBlockDropItem, which fires later in the same tick.
    private final Map<Location, UUID> pendingBreaks = new HashMap<>();

    // Hanging entities (item frames, paintings, lead knots) attached to a block are not part of
    // that block's drops - the server only notices their support is gone on a periodic survival
    // check, which can lag the break by up to 100 ticks. So SafeMiner-broken blocks are also
    // remembered here for a short window, keyed by block location -> (miner, expiry timestamp),
    // so the resulting HangingBreakEvent can still be traced back to the player who caused it.
    private static final long HANGING_WINDOW_MS = 10_000L;
    private final Map<Location, RecentBreak> recentBreaks = new HashMap<>();

    private record RecentBreak(UUID miner, long expiresAt) {}

    public SafeMiner(Main main) {
        this.main = main;
        this.worldGuardPlugin = main.getWorldGuardPlugin();
    }

    public boolean hasTool (Player p) {
        // Get the player's tool(s)
        ItemStack tool = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.SAFE-MINER.Enchantment-Apply-Item");

        // Check if the player has a tool in their hand
        return tool != null && armorMat.contains(tool.getType().toString());
    }

    // Run at HIGHEST + ignoreCancelled so that any protection / block-respawn plugin
    // (which typically cancels the BlockBreakEvent) has already had its say. Otherwise
    // SafeMiner would hand the player the drops before another plugin cancels/respawns
    // the block, resulting in duplication.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        // Chain enchants (Vein Seeker, Tree Hugger) break their extra blocks themselves and route
        // the drops to the player directly, so no BlockDropItemEvent ever follows. Flagging them
        // here would queue a redirect - and a cleanup task - per block that could never be
        // consumed; a full 256-block vein meant 255 wasted scheduler tasks in one tick.
        if (e instanceof ChainBlockBreakEvent) return;
        // Grabbing the player
        Player player = e.getPlayer();
        // Grabbing the broken block
        Block blockMined = e.getBlock();

        //WorldGuard Checks
        if(worldGuardPlugin != null && worldGuardPlugin.isEnabled() && !player.isOp())
        {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(blockMined.getWorld()));

            if (regions != null) {
                ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.asBlockVector(blockMined.getLocation()));

                for (ProtectedRegion region : set) {
                    if (!region.getMembers().contains(worldGuardPlugin.wrapPlayer(player)) &&
                            !region.getOwners().contains(worldGuardPlugin.wrapPlayer(player))) {
                        // Cancel the event if the player is not a member or owner of the region
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // Check if the player has an enchanted tool
        if (!hasTool(player)) return;

        // Get Enchantment Enabled Status
        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.SAFE-MINER.Enchantment-Enabled");
        // if Enchantment Enabled = false return.
        if (!isEnchantmentEnabled) return;

        for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(player.getInventory().getItemInMainHand())) {
            if (enchant.name.contains("SAFE-MINER")) {
                // PUT ENCHANT LOGIC HERE
                if (isAltarOfCirce(blockMined)) {
                    // Create an instance of CreateAltarOfCirce and call the method
                    CreateAltarOfCirce createAltarOfCirce = new CreateAltarOfCirce(main);
                    // Drop the custom altar instead
                    createAltarOfCirce.CreateAltarOfCirceItem(1, player);
                    // Prevent default drops
                    e.setDropItems(false);
                    return;
                }

                // Flag the break so onBlockDropItem can redirect the drops into the player's
                // inventory. We deliberately do NOT use setDropItems(false) + Block#getDrops():
                // the server's drop capture covers the whole removal, so suppressing it also
                // voids items produced by the removal's side effects - a torch, door, rail or
                // sign attached to the block popping off, and container contents. Those never
                // appear in getDrops(), so they were being deleted outright.
                pendingBreaks.put(blockMined.getLocation(), player.getUniqueId());
                // Separate, longer-lived record for hanging entities knocked loose by this break.
                pruneRecentBreaks();
                recentBreaks.put(blockMined.getLocation(),
                        new RecentBreak(player.getUniqueId(), System.currentTimeMillis() + HANGING_WINDOW_MS));
                // Safety net: if the block yields no drops at all, BlockDropItemEvent never
                // fires, so drop the flag on the next tick rather than leaking it.
                Location flagged = blockMined.getLocation();
                main.getServer().getScheduler().runTask(main, () -> pendingBreaks.remove(flagged));
                return;
                //END ENCHANT LOGIC
            }
        }
    }

    // Fires after the block (and anything the break knocked loose) has produced its items,
    // but before those item entities are added to the world.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent e) {
        Location location = e.getBlock().getLocation();
        UUID miner = pendingBreaks.get(location);
        if (miner == null || !miner.equals(e.getPlayer().getUniqueId())) return;
        pendingBreaks.remove(location);

        List<ItemStack> drops = new ArrayList<>();
        for (Item item : e.getItems()) {
            drops.add(item.getItemStack());
        }

        // Clearing the list stops the server from spawning the entities at all
        e.getItems().clear();

        giveOrDropItems(e.getPlayer(), drops, location);
    }

    // Item frames, paintings and lead knots are entities, so they never produce a
    // BlockDropItemEvent. Handles both ways they die: the player breaking one directly with a
    // SafeMiner tool, and one popping off because SafeMiner removed the block holding it up.
    // HangingBreakByEntityEvent extends HangingBreakEvent, so both arrive here.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent e) {
        Hanging hanging = e.getEntity();
        Player player = null;

        if (e instanceof HangingBreakByEntityEvent byEntity) {
            if (byEntity.getRemover() instanceof Player remover && hasSafeMiner(remover)) {
                player = remover;
            }
        } else if (e.getCause() == HangingBreakEvent.RemoveCause.PHYSICS
                || e.getCause() == HangingBreakEvent.RemoveCause.OBSTRUCTION) {
            player = recentMiner(hanging);
        }

        if (player == null) return;
        // Creative breaks yield nothing in vanilla; leave that alone.
        if (player.getGameMode() == GameMode.CREATIVE) return;

        List<ItemStack> drops = hangingDrops(hanging);
        // Nothing to collect (e.g. a fixed frame) - let the server handle it normally.
        if (drops.isEmpty()) return;

        // There is no setDropItems() for hanging entities, so suppress the vanilla removal and
        // do it ourselves; cancelling alone would leave the entity floating in mid-air.
        e.setCancelled(true);
        hanging.remove();

        giveOrDropItems(player, drops, hanging.getLocation());
    }

    // Punching an item frame that holds an item pops the item out and leaves the frame standing -
    // that path fires no HangingBreakEvent at all, only entity damage.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFrameDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof ItemFrame frame)) return;
        if (!(e.getDamager() instanceof Player player)) return;
        if (frame.isFixed()) return;

        ItemStack content = frame.getItem();
        // Empty frame: this hit destroys the frame itself, which onHangingBreak covers.
        if (content == null || content.getType().isAir()) return;

        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (!hasSafeMiner(player)) return;

        e.setCancelled(true);
        // Clears the frame without dropping the item, playing the usual removal sound.
        frame.setItem(null, true);

        giveOrDropItems(player, List.of(content.clone()), frame.getLocation());
    }

    // What the hanging entity would have dropped on the floor.
    private List<ItemStack> hangingDrops(Hanging hanging) {
        List<ItemStack> drops = new ArrayList<>();

        if (hanging instanceof ItemFrame frame) {
            if (frame.isFixed()) return drops;
            drops.add(new ItemStack(frame instanceof GlowItemFrame ? Material.GLOW_ITEM_FRAME : Material.ITEM_FRAME));
            ItemStack content = frame.getItem();
            if (content != null && !content.getType().isAir()) drops.add(content.clone());
        } else if (hanging instanceof Painting) {
            drops.add(new ItemStack(Material.PAINTING));
        } else if (hanging instanceof LeashHitch) {
            drops.add(new ItemStack(Material.LEAD));
        }

        return drops;
    }

    // The player who recently broke the block this entity was hanging on, if any.
    private Player recentMiner(Hanging hanging) {
        pruneRecentBreaks();
        Location support = hanging.getLocation().getBlock().getRelative(hanging.getAttachedFace()).getLocation();
        RecentBreak recent = recentBreaks.get(support);
        if (recent == null) return null;

        Player player = main.getServer().getPlayer(recent.miner());
        return (player != null && player.isOnline()) ? player : null;
    }

    private void pruneRecentBreaks() {
        long now = System.currentTimeMillis();
        recentBreaks.values().removeIf(recent -> recent.expiresAt() <= now);
    }

    // Whether the player is holding a tool carrying a usable Safe Miner enchant.
    private boolean hasSafeMiner(Player player) {
        if (!hasTool(player)) return false;
        if (!main.getEnchantmentsConfig().getBoolean("Enchantments.SAFE-MINER.Enchantment-Enabled")) return false;

        for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(player.getInventory().getItemInMainHand())) {
            if (enchant.name.contains("SAFE-MINER")) return true;
        }
        return false;
    }

    private boolean isAltarOfCirce(Block block) {
        if (block.getType() != Material.ENCHANTING_TABLE) return false;
        if (!(block.getState() instanceof TileState tileState)) return false;

        PersistentDataContainer blockPDC = tileState.getPersistentDataContainer();
        return "altar_of_circe".equals(blockPDC.get(Main.customAltarOfCirceKeys, PersistentDataType.STRING));
    }

    private void giveOrDropItems(Player player, Collection<ItemStack> items, Location dropLocation) {
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) continue;

            HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item);
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(dropLocation, leftover);
            }
        }
    }

}
