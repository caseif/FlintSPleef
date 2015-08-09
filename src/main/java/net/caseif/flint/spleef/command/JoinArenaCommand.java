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

import net.caseif.flint.arena.Arena;
import net.caseif.flint.exception.round.RoundJoinException;
import net.caseif.flint.round.Round;
import net.caseif.flint.spleef.Main;

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
        if (sender.hasPermission("fs.play")) {
            if (sender instanceof Player) {
                if (args.length > 1) {
                    String arenaName = args[1];
                    Optional<Arena> arena = Main.getMinigame().getArena(arenaName);
                    if (arena.isPresent()) {
                        Round round = arena.get().getRound().orNull();
                        if (round == null) {
                            round = arena.get().createRound();
                        }
                        if (!round.getLifecycleStage().getId().equals(Main.PLAYING_STAGE_ID)) {
                            try {
                                round.addChallenger(((Player) sender).getUniqueId());
                                sender.sendMessage(INFO_COLOR + "[FlintSpleef] Successfully joined arena " + EM_COLOR
                                        + arena.get().getName());
                            } catch (RoundJoinException ex) {
                                sender.sendMessage(ERROR_COLOR + "[FlintSpleef] Failed to join: " + ex.getMessage());
                            }
                        } else {
                            sender.sendMessage(ERROR_COLOR + "[FlintSpleef] You may not join a round in progress");
                        }
                    } else {
                        sender.sendMessage(ERROR_COLOR + "[FlintSpleef] No arena by ID " + EM_COLOR + arenaName
                                + ERROR_COLOR + " exists");
                    }
                } else {
                    sender.sendMessage(ERROR_COLOR + "[FlintSpleef] Too few arguments! Usage: /fs join [arena]");
                }
            } else {
                sender.sendMessage(ERROR_COLOR + "[FlintSpleef] You must be an in-game player to use this command");
            }
        } else {
            sender.sendMessage(ERROR_COLOR + "[FlintSpleef] You do not have permission to use this command");
        }
    }

}
