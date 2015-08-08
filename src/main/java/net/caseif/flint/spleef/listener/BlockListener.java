/*
 * New BSD License (BSD-new)
 *
 * Copyright (c) 2015 Maxim Roncacé
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
