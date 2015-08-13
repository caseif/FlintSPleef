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

import static net.caseif.flint.spleef.Main.ERROR_COLOR;
import static net.caseif.flint.spleef.Main.PREFIX;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.spleef.Main;

import com.google.common.base.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handler for the arena leave command.
 *
 * @author Max Roncacé
 */
public class LeaveArenaCommand {

    public static void handle(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Optional<Challenger> challenger = Main.getMinigame().getChallenger(((Player) sender).getUniqueId());
            if (challenger.isPresent()) {
                challenger.get().removeFromRound();
                sender.sendMessage("You have been successfully removed from the round");
            } else {
                sender.sendMessage(PREFIX + ERROR_COLOR + "You must be an in-game player to use this command");
            }
        }
    }

}
