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
package net.caseif.flint.spleef.command;

import static net.caseif.flint.spleef.Main.EM_COLOR;
import static net.caseif.flint.spleef.Main.ERROR_COLOR;
import static net.caseif.flint.spleef.Main.INFO_COLOR;
import static net.caseif.flint.spleef.Main.LOCALE_MANAGER;
import static net.caseif.flint.spleef.Main.PREFIX;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.exception.round.RoundJoinException;
import net.caseif.flint.round.Round;
import net.caseif.flint.spleef.Main;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handler for the arena join command.
 *
 * @author Max Roncacé
 */
public class JoinArenaCommand {

    public static void handle(CommandSender sender, String[] args) {
        if (sender.hasPermission("flintspleef.play")) {
            if (sender instanceof Player) {
                if (args.length > 1) {
                    String[] idArray = new String[args.length - 1];
                    System.arraycopy(args, 1, idArray, 0, idArray.length);
                    String arenaId = Joiner.on(" ").join(idArray);
                    Optional<Arena> arena = Main.getMinigame().getArena(arenaId);
                    if (arena.isPresent()) {
                        Round round = arena.get().getRound().orNull();
                        if (round == null) {
                            round = arena.get().createRound();
                        }
                        if (!round.getLifecycleStage().getId().equals(Main.PLAYING_STAGE_ID)) {
                            try {
                                round.addChallenger(((Player) sender).getUniqueId());
                                LOCALE_MANAGER.getLocalizable("message.info.command.join.success")
                                        .withPrefix(PREFIX + INFO_COLOR)
                                        .withReplacements(EM_COLOR + arena.get().getName()).sendTo(sender);
                            } catch (RoundJoinException ex) {
                                LOCALE_MANAGER.getLocalizable("message.error.command.join.exception")
                                        .withPrefix(PREFIX + ERROR_COLOR).withReplacements(ex.getMessage())
                                        .sendTo(sender);
                            }
                        } else {
                            LOCALE_MANAGER.getLocalizable("message.error.command.join.progress")
                                    .withPrefix(PREFIX + ERROR_COLOR).sendTo(sender);
                        }
                    } else {
                        LOCALE_MANAGER.getLocalizable("message.error.command.join.not-found")
                                .withPrefix(PREFIX + ERROR_COLOR).withReplacements(EM_COLOR + arenaId + ERROR_COLOR)
                                .sendTo(sender);
                    }
                } else {
                    LOCALE_MANAGER.getLocalizable("message.error.general.too-few-args").withPrefix(PREFIX + ERROR_COLOR)
                            .sendTo(sender);
                    LOCALE_MANAGER.getLocalizable("message.error.general.usage").withPrefix(PREFIX + ERROR_COLOR)
                            .withReplacements("/fs join [arena]").sendTo(sender);
                }
            } else {
                sender.sendMessage(LOCALE_MANAGER.getLocalizable("message.error.general.in-game")
                        .withPrefix(PREFIX + ERROR_COLOR).localize());
            }
        } else {
            LOCALE_MANAGER.getLocalizable("message.error.general.permission").withPrefix(PREFIX + ERROR_COLOR)
                    .sendTo(sender);
        }
    }

}
