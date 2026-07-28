package com.atlasplugins.atlasenchants.listeners.blockevents;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Fired by the chain-mining enchants (Vein Seeker, Tree Hugger) for every block they break
 * <em>beyond</em> the block the player actually hit.
 * <p>
 * It deliberately declares no {@code HandlerList} of its own, so it inherits
 * {@link BlockBreakEvent}'s. Protection, claim and logging plugins registered against
 * {@code BlockBreakEvent} receive it exactly like a normal break and can cancel it - that is the
 * whole point of firing it.
 * <p>
 * Our own listeners use {@code instanceof ChainBlockBreakEvent} to opt out of work that should
 * only ever happen for the block the player struck. Miner's Touch must not mint a spawner for
 * every block in a vein, and Safe Miner must not queue a redirect for blocks whose drops the
 * chain enchant is already handling itself.
 */
public class ChainBlockBreakEvent extends BlockBreakEvent {

    public ChainBlockBreakEvent(Block block, Player player) {
        super(block, player);
    }
}
