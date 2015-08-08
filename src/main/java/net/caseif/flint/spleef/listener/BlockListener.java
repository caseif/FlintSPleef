package net.caseif.flint.spleef.listener;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.spleef.Main;

import com.google.common.base.Optional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;

/**
 * Listener for block-related events.
 *
 * @author Max Roncacé
 */
public class BlockListener implements Listener {

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        // check if the damager is a challenger
        Optional<Challenger> challenger = Main.getMinigame().getChallenger(event.getPlayer().getUniqueId());
        if (challenger.isPresent()) { // damager is a challenger
            // check if the round hasn't started yet
            if (!challenger.get().getRound().getLifecycleStage().getId().equals(Main.PLAYING_STAGE_ID)) {
                event.setCancelled(true); // can't break blocks in advance
            }
            // check if they're holding a shovel
            if (!Main.SHOVELS.contains(event.getItemInHand().getType())) {
                event.setCancelled(true); // can't break blocks without a shovel
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // check if the damager is a challenger
        Optional<Challenger> challenger = Main.getMinigame().getChallenger(event.getPlayer().getUniqueId());
        if (challenger.isPresent()) { // damager is a challenger
            event.getBlock().getDrops().clear(); // clear the drops
            event.getPlayer().getItemInHand().setDurability((short) 0); // avoid damaging the shovel
        }
    }

}
