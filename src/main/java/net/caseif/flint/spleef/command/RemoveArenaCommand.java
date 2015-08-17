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
import static net.caseif.flint.spleef.Main.PREFIX;

import net.caseif.flint.arena.Arena;
import net.caseif.flint.spleef.Main;

import com.google.common.base.Optional;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for the remove arena command.
 *
 * @author Max Roncacé
 */
public class RemoveArenaCommand {

    private static final List<String> warned = new ArrayList<>();

    public static void handle(CommandSender sender, String[] args) {
        if (sender.hasPermission("flintspleef.arena.remove")) {
            if (args.length > 2) {
                Optional<Arena> arena = Main.getMinigame().getArena(args[2]);
                if (arena.isPresent()) {
                    if (!arena.get().getRound().isPresent() || warned.contains(sender.getName())) {
                        if (arena.get().getRound().isPresent()) {
                            sender.sendMessage(PREFIX + INFO_COLOR + "Ending round first...");
                            arena.get().getRound().get().end();
                        }
                        warned.remove(sender.getName());
                        String id = arena.get().getId();
                        String name = arena.get().getName();
                        Main.getMinigame().removeArena(arena.get());
                        sender.sendMessage(PREFIX + INFO_COLOR + "Successfully removed arena " + EM_COLOR
                                + name + INFO_COLOR + " (ID: " + EM_COLOR + id + INFO_COLOR + ")");
                    } else {
                        sender.sendMessage(PREFIX + ERROR_COLOR + "Arena " + EM_COLOR + arena.get().getName()
                                + ERROR_COLOR + " contains an active round. If you still wish to remove it, run the "
                                + "command again and the round will be ended and the arena removed.");
                        warned.add(sender.getName());
                    }
                } else {
                    sender.sendMessage(PREFIX + ERROR_COLOR + "Arena with ID " + EM_COLOR + args[2] + ERROR_COLOR
                            + " does not exist");
                }
            } else {
                sender.sendMessage(PREFIX + ERROR_COLOR + "Too few arguments! Usage: /fs arena remove [arena]");
            }
        } else {
            sender.sendMessage(PREFIX + ERROR_COLOR + "You do not have permission to use this command");
        }
    }

}
