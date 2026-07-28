package com.atlasplugins.atlasenchants.enchants.tools;

import com.atlasplugins.atlasenchants.Main;
import com.atlasplugins.atlasenchants.listeners.blockevents.ChainBlockBreakEvent;
import com.atlasplugins.atlasenchants.utils.EnchantUtils;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class VeinSeeker implements Listener {

    private Main main;
    private WorldGuardPlugin worldGuardPlugin;

    private static final int MAX_BLOCKS = 256;

    // Set while we are breaking chain blocks. Each chain block gets its own BlockBreakEvent so
    // protection plugins can veto it, and those events come back to this listener - without the
    // guard a vein would recurse into itself.
    private boolean breakingChain = false;

    public VeinSeeker (Main main) {
        this.main = main;
        this.worldGuardPlugin = main.getWorldGuardPlugin();
    }

    public boolean hasTool (Player p) {
        // Get the player's tool(s)
        ItemStack tool = p.getInventory().getItemInMainHand();

        // Get the list of items the Enchant can be applied to from the config
        List<String> armorMat = main.getEnchantmentsConfig().getStringList("Enchantments.VEIN-SEEKER.Enchantment-Apply-Item");

        // Check if the player has a tool in their hand
        return tool != null && armorMat.contains(tool.getType().toString());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.isCancelled()) return;
        // Don't let our own chain-break events start another vein.
        if(breakingChain) return;
        //Grabbing the player
        Player p = e.getPlayer();
        // Grabbing the broken block
        Block blockBroken = e.getBlock();

        // No region check on the struck block: WorldGuard already vetoes that break through its own
        // BlockBreakEvent handler, and the check that used to sit here cancelled the event for
        // every player who was not a region member - regardless of the region's build flag and
        // regardless of whether they had this enchant at all. Chain blocks are still checked below.

        // Check if the player has an enchanted tool
        if(!hasTool(p)) return;

        // Get Enchantment Enabled Status
        boolean isEnchantmentEnabled = main.getEnchantmentsConfig().getBoolean("Enchantments.VEIN-SEEKER.Enchantment-Enabled");
        // if Enchantment Enabled = false return.
        if(!isEnchantmentEnabled) return;

        // The enchant is opt-in per break: stood up, the pickaxe behaves like an unenchanted one
        // so the player can mine a single ore without tearing out the whole vein.
        if(main.getEnchantmentsConfig().getBoolean("Enchantments.VEIN-SEEKER.Enchantment-Require-Sneak", true)
                && !p.isSneaking()) return;

        // Get the block list.
        List<String> ores = main.getEnchantmentsConfig().getStringList("Enchantments.VEIN-SEEKER.VeinSeeker-Block-List");

        for (EnchantUtils.EnchantData enchant : EnchantUtils.parseEnchants(p.getInventory().getItemInMainHand())) {
            if (enchant.name.contains("VEIN-SEEKER")) {
                // PUT ENCHANT LOGIC HERE
                ItemStack tool = p.getInventory().getItemInMainHand();

                if (ores.contains(blockBroken.getType().toString())) {
                    mineOres(blockBroken, e.getExpToDrop(), tool, p, ores);
                }
                // A tool should only vein-mine once even if the PDC somehow holds a duplicate
                // VEIN-SEEKER entry, otherwise the durability cost is charged twice.
                break;
                //END ENCHANT LOGIC
            }
        }
    }

    /**
     * Whether WorldGuard permits this player to break this block.
     *
     * <p>Asks WorldGuard for the BUILD flag rather than testing region membership. Membership is
     * the wrong question: a region can grant building to everyone, or to a group the player is in,
     * without listing them as a member or owner.
     */
    private boolean canBuild(Player p, Block block) {
        if (worldGuardPlugin == null || !worldGuardPlugin.isEnabled()) return true;

        LocalPlayer localPlayer = worldGuardPlugin.wrapPlayer(p);

        // Anyone WorldGuard already exempts (staff with bypass, /rg bypass) is not re-checked here.
        if (WorldGuard.getInstance().getPlatform().getSessionManager()
                .hasBypass(localPlayer, BukkitAdapter.adapt(block.getWorld()))) {
            return true;
        }

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return query.testState(BukkitAdapter.adapt(block.getLocation()), localPlayer, Flags.BUILD);
    }

    // Breaks the vein around the start block. The start block itself is left to the original
    // BlockBreakEvent, so only the chain blocks are handled here.
    private void mineOres(Block startBlock, int blockXP, ItemStack tool, Player p, List<String> ores) {
        Set<Block> oresToMine = new LinkedHashSet<>();
        findOres(startBlock, oresToMine, ores);

        boolean hasSafeMiner = hasSafeMinerEnchant(tool);
        // Vein Seeker ignores Unbreaking by design (see the enchant lore), but it must still
        // respect the tool actually running out of durability mid-vein.
        int durabilityBudget = remainingDurability(tool);
        int blocksMined = 0;
        boolean toolBroke = false;

        breakingChain = true;
        try {
            for (Block ore : oresToMine) {
                // Skip the start block, the BlockBreakEvent (and SafeMiner if present) handles it
                if (ore.equals(startBlock)) continue;
                // Stop once the tool has nothing left; the last point of durability is spent on
                // the block that breaks it.
                if (durabilityBudget <= 0) break;

                // Our own region check only covered the block the player struck, so a vein
                // crossing a region border ignored it from the second block onwards.
                if (!canBuild(p, ore)) continue;

                // Give every other plugin a real event to veto. Protection, claim and logging
                // plugins never saw these blocks when they were broken directly, so a vein
                // crossing a claim border was mined straight through it, unlogged.
                BlockBreakEvent chainEvent = new ChainBlockBreakEvent(ore, p);
                chainEvent.setExpToDrop(0);
                main.getServer().getPluginManager().callEvent(chainEvent);
                if (chainEvent.isCancelled()) continue;

                if (hasSafeMiner && chainEvent.isDropItems()) {
                    // Collect drops and send to player's inventory instead of dropping on ground.
                    // Only reachable on legacy items - SAFE-MINER is blacklisted against
                    // VEIN-SEEKER in enchantments.yml. Ores have no container contents or
                    // attached-block drops, so Block#getDrops covers everything they yield.
                    Collection<ItemStack> drops = ore.getDrops(tool, p);
                    ore.setType(Material.AIR);
                    giveOrDropItems(p, drops, ore);
                } else if (chainEvent.isDropItems()) {
                    ore.breakNaturally(tool);
                } else {
                    ore.setType(Material.AIR);
                }

                blocksMined++;
                durabilityBudget--;
                if (durabilityBudget <= 0) {
                    toolBroke = true;
                }
            }
        } finally {
            breakingChain = false;
        }

        applyDurability(p, tool, blocksMined, toolBroke);
        spawnChainExperience(startBlock, blockXP, blocksMined);
    }

    // Damage left before the tool breaks, or Integer.MAX_VALUE for tools that can't be damaged.
    private int remainingDurability(ItemStack tool) {
        if (tool == null) return 0;
        short max = tool.getType().getMaxDurability();
        if (max <= 0) return Integer.MAX_VALUE;
        if (!(tool.getItemMeta() instanceof Damageable damageable)) return Integer.MAX_VALUE;
        if (damageable.isUnbreakable()) return Integer.MAX_VALUE;
        return Math.max(0, max - damageable.getDamage());
    }

    private void applyDurability(Player p, ItemStack tool, int blocksMined, boolean toolBroke) {
        if (blocksMined <= 0 || tool == null) return;
        short max = tool.getType().getMaxDurability();
        if (max <= 0) return;

        ItemMeta meta = tool.getItemMeta();
        if (!(meta instanceof Damageable damageable) || damageable.isUnbreakable()) return;

        if (toolBroke) {
            // Mirror vanilla: fire the break event, remove the item and play the snap.
            main.getServer().getPluginManager().callEvent(new PlayerItemBreakEvent(p, tool));
            p.getInventory().setItemInMainHand(null);
            Sound breakSound = Main.getSound("ENTITY_ITEM_BREAK");
            if (breakSound != null) {
                p.playSound(p.getLocation(), breakSound, 1f, 1f);
            }
            return;
        }

        // Clamped so the tool can never end up past its own max damage, which left it in a
        // permanently "broken but usable" state.
        damageable.setDamage(Math.min(max - 1, damageable.getDamage() + blocksMined));
        tool.setItemMeta(damageable);
    }

    // Spawn a single XP orb for chain blocks (start block XP is handled by the event).
    private void spawnChainExperience(Block startBlock, int blockXP, int blocksMined) {
        if (blockXP <= 0 || blocksMined <= 0) return;
        long totalXP = (long) blockXP * blocksMined;
        int amount = (int) Math.min(Integer.MAX_VALUE, totalXP);
        if (amount <= 0) return;

        ExperienceOrb experienceOrb = startBlock.getWorld().spawn(startBlock.getLocation(), ExperienceOrb.class);
        experienceOrb.setExperience(amount);
    }

    private void giveOrDropItems(Player p, Collection<ItemStack> items, Block source) {
        for (ItemStack item : items) {
            if (item == null || item.getType().isAir()) continue;
            HashMap<Integer, ItemStack> leftovers = p.getInventory().addItem(item);
            for (ItemStack leftover : leftovers.values()) {
                source.getWorld().dropItemNaturally(source.getLocation(), leftover);
            }
        }
    }

    private boolean hasSafeMinerEnchant(ItemStack tool) {
        if (tool == null || tool.getItemMeta() == null) return false;
        String enchantData = tool.getItemMeta().getPersistentDataContainer().get(Main.customEnchantKeys, PersistentDataType.STRING);
        return enchantData != null && enchantData.contains("SAFE-MINER");
    }

    // Iterative block search with a max block cap to prevent stack overflow.
    private void findOres(Block start, Set<Block> oresToMine, List<String> ores) {
        Set<Block> seen = new HashSet<>();
        Deque<Block> queue = new ArrayDeque<>();
        queue.add(start);
        seen.add(start);

        while (!queue.isEmpty() && oresToMine.size() < MAX_BLOCKS) {
            Block block = queue.poll();
            oresToMine.add(block);

            for (Block nblock : neighbours(block)) {
                // `seen` also covers blocks already queued - the old check only looked at the
                // result set, so the same block could be queued dozens of times.
                if (!seen.add(nblock)) continue;
                if (!ores.contains(nblock.getType().toString())) continue;
                queue.add(nblock);
            }
        }
    }

    // The 26 blocks surrounding `block`, skipping anything outside the world's build limits or
    // in an unloaded chunk (reading those forces a synchronous chunk load mid-tick).
    private List<Block> neighbours(Block block) {
        World world = block.getWorld();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;

        List<Block> blocks = new ArrayList<>(26);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    int y = block.getY() + dy;
                    if (y < minY || y > maxY) continue;

                    int x = block.getX() + dx;
                    int z = block.getZ() + dz;
                    if (!world.isChunkLoaded(x >> 4, z >> 4)) continue;

                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }
}
