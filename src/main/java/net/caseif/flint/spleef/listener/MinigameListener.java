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

import static net.caseif.flint.spleef.Main.EM_COLOR;
import static net.caseif.flint.spleef.Main.INFO_COLOR;
import static net.caseif.flint.spleef.Main.LOCALE_MANAGER;
import static net.caseif.flint.spleef.Main.MIN_PLAYERS;
import static net.caseif.flint.spleef.Main.PLAYING_STAGE_ID;
import static net.caseif.flint.spleef.Main.PREFIX;
import static net.caseif.flint.spleef.Main.PREPARING_STAGE_ID;
import static net.caseif.flint.spleef.Main.WAITING_STAGE_ID;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.event.lobby.PlayerClickLobbySignEvent;
import net.caseif.flint.event.round.RoundChangeLifecycleStageEvent;
import net.caseif.flint.event.round.RoundTimerTickEvent;
import net.caseif.flint.event.round.challenger.ChallengerJoinRoundEvent;
import net.caseif.flint.spleef.Main;
import net.caseif.flint.spleef.command.JoinArenaCommand;
import net.caseif.flint.util.physical.Location3D;
import net.caseif.rosetta.Localizable;

import com.google.common.eventbus.Subscribe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;

/**
 * Listener for minigame-related events.
 *
 * @author Max Roncacé
 */
public class MinigameListener implements Listener {

    @Subscribe
    public void onChallengerJoinRound(ChallengerJoinRoundEvent event) {
        // check if round is in progress
        if (event.getRound().getLifecycleStage().getId().equals(Main.PLAYING_STAGE_ID)) {
            event.getChallenger().setSpectating(true); // can't just join in the middle of a round
        } else if (event.getRound().getLifecycleStage().getId().equals(Main.WAITING_STAGE_ID)
                && event.getRound().getChallengers().size() >= MIN_PLAYERS) {
            event.getRound().nextLifecycleStage(); // advance to preparation stage
        }
    }

    @Subscribe
    public void onRoundChangeLifecycleStage(RoundChangeLifecycleStageEvent event) {
        // check if round is in progress
        if (event.getStageAfter().getId().equals(Main.PLAYING_STAGE_ID)) {
            Localizable msg = LOCALE_MANAGER.getLocalizable("message.info.event.start").withPrefix(PREFIX + INFO_COLOR);
            for (Challenger ch : event.getRound().getChallengers()) {
                msg.sendTo(Bukkit.getPlayer(ch.getUniqueId()));
            }
            // iterate challengers
            for (Challenger challenger : event.getRound().getChallengers()) {
                // give 'em all shovels
                Bukkit.getPlayer(challenger.getUniqueId()).getInventory().addItem(Main.SHOVEL);
            }
        } else if (event.getStageAfter().getId().equals(Main.PREPARING_STAGE_ID)) {
            Localizable msg = LOCALE_MANAGER.getLocalizable("message.info.event.prepare")
                    .withPrefix(PREFIX + INFO_COLOR);
            for (Challenger ch : event.getRound().getChallengers()) {
                msg.sendTo(Bukkit.getPlayer(ch.getUniqueId()));
            }
        }
    }

    @Subscribe
    public void onRoundTimerTick(RoundTimerTickEvent event) {
        if (event.getRound().getRemainingTime() % 10 == 0 && event.getRound().getRemainingTime() > 0) {
            if (!event.getRound().getLifecycleStage().getId().equals(WAITING_STAGE_ID)) {
                Localizable msg = LOCALE_MANAGER.getLocalizable(
                        event.getRound().getLifecycleStage().getId().equals(PREPARING_STAGE_ID)
                                ? "message.info.event.begin-countdown"
                                : "message.info.event.end-countdown")
                        .withPrefix(PREFIX + INFO_COLOR).withReplacements(event.getRound().getRemainingTime() + "");
                for (Challenger ch : event.getRound().getChallengers()) {
                    msg.sendTo(Bukkit.getPlayer(ch.getUniqueId()));
                }
            }
        }

        // iterate the challengers once per round tick
        for (Challenger challenger : event.getRound().getChallengers()) {
            // check whether the challenger is below y=0 (the void)
            if (Bukkit.getPlayer(challenger.getUniqueId()).getLocation().getY() < 0) {
                if (event.getRound().getLifecycleStage().getId().equals(PLAYING_STAGE_ID)) {
                    challenger.removeFromRound(); // they lost
                } else {
                    Location3D spawn = event.getRound().getArena().getSpawnPoints().get(0);
                    World w = Bukkit.getWorld(spawn.getWorld().get());
                    Bukkit.getPlayer(challenger.getUniqueId())
                            .teleport(new Location(w, spawn.getX(), spawn.getY(), spawn.getZ()));
                }
            }
        }

        if (event.getRound().getChallengers().size() <= 1) {
            if (event.getRound().getLifecycleStage().getId().equals(PLAYING_STAGE_ID)) {
                if (event.getRound().getChallengers().size() == 1) {
                    LOCALE_MANAGER.getLocalizable("message.info.event.win").withPrefix(PREFIX + INFO_COLOR)
                            .withReplacements(
                                    EM_COLOR + event.getRound().getChallengers().toArray(new Challenger[1])[0].getName()
                                            + INFO_COLOR,
                                    EM_COLOR + event.getRound().getArena().getName() + INFO_COLOR)
                            .broadcast();
                    event.getRound().end();
                } else if (event.getRound().getChallengers().isEmpty()) {
                    event.getRound().end();
                }
            } else if (event.getRound().getLifecycleStage().getId().equals(PREPARING_STAGE_ID)) {
                event.getRound().setLifecycleStage(event.getRound().getLifecycleStage(WAITING_STAGE_ID).get());
            }
        }
    }

    @Subscribe
    public void onPlayerClickLobbySign(PlayerClickLobbySignEvent event) {
        if (Bukkit.getPlayer(event.getPlayer()).hasPermission("flintspleef.play")) {
            // simulate a join command because I'm a lazy bastard
            JoinArenaCommand.handle(Bukkit.getPlayer(event.getPlayer()),
                    new String[]{"join", event.getLobbySign().getArena().getId()});
        }
    }

}
