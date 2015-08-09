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
import static net.caseif.flint.spleef.Main.ERROR_COLOR;
import static net.caseif.flint.spleef.Main.INFO_COLOR;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARDS;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_FIRST_BOUND;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_ID;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_INFO;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_NAME;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_SECOND_BOUND;
import static net.caseif.flint.spleef.command.CreateArenaCommand.WIZARD_SPAWN_POINT;

import net.caseif.flint.spleef.Main;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player-related events.
 *
 * @author Max Roncacé
 */
public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (WIZARDS.containsKey(event.getPlayer().getUniqueId())) {
            int stage = WIZARDS.get(event.getPlayer().getUniqueId());
            if (event.getMessage().equalsIgnoreCase("cancel")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ERROR_COLOR + "[FlintSpleef] Arena creation cancelled.");
                return;
            }
            event.setCancelled(true);
            switch (stage) {
                case WIZARD_ID:
                    increment(event.getPlayer());
                    if (!Main.getMinigame().getArena(event.getMessage()).isPresent()) {
                        WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_ID] = event.getMessage();
                        event.getPlayer().sendMessage(INFO_COLOR + "[FlintSpleef] Okay! Your arena will be created "
                                + "with ID " + EM_COLOR + event.getMessage().toLowerCase() + INFO_COLOR + ". Next, "
                                + "please enter the display name for the new arena.");
                    } else {
                        event.getPlayer().sendMessage(ERROR_COLOR + "[FlintSpleef] An arena with that ID already "
                                + "exists! Please enter a different ID for the new arena.");
                    }
                    break;
                case WIZARD_NAME:
                    increment(event.getPlayer());
                    WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_NAME] = event.getMessage();
                    event.getPlayer().sendMessage(INFO_COLOR + "[FlintSpleef] Okay! Your arena will be given display "
                            + "name " + EM_COLOR + event.getMessage() + INFO_COLOR + ". Next, please click the block "
                            + "you would like to use as the first corner of the arena's boundary.");
                    break;
                case WIZARD_SPAWN_POINT:
                    if (event.getMessage().equalsIgnoreCase("ok")) {
                        if (event.getPlayer().getWorld().getName().equals(
                                ((Location3D) WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_FIRST_BOUND])
                                        .getWorld().get()
                        )) {
                            Location3D spawn = new Location3D(event.getPlayer().getWorld().getName(),
                                    event.getPlayer().getLocation().getX(),
                                    event.getPlayer().getLocation().getY(),
                                    event.getPlayer().getLocation().getZ());
                            Object[] info = WIZARD_INFO.get(event.getPlayer().getUniqueId());
                            Main.getMinigame().createArena((String) info[WIZARD_ID], (String) info[WIZARD_NAME],
                                    spawn, new Boundary((Location3D) info[WIZARD_FIRST_BOUND],
                                            (Location3D) info[WIZARD_SECOND_BOUND]));
                            event.getPlayer().sendMessage(INFO_COLOR + "[FlintSpleef] The arena was successfully "
                                    + "created! You may join it by typing " + EM_COLOR + "/fs join "
                                    + ((String) info[WIZARD_ID]).toLowerCase() + INFO_COLOR + ".");
                            WIZARDS.remove(event.getPlayer().getUniqueId());
                            WIZARD_INFO.remove(event.getPlayer().getUniqueId());
                        } else {
                            event.getPlayer().sendMessage(ERROR_COLOR + "[FlintSpleef] The spawn point must be in the "
                                    + "same world as the boundary");
                        }
                        break;
                    }
                    // fall-through is intentional
                default:
                    event.setCancelled(false);
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (WIZARDS.containsKey(event.getPlayer().getUniqueId())) {
                int stage = WIZARDS.get(event.getPlayer().getUniqueId());
                event.setCancelled(true);
                Block c = event.getClickedBlock();
                switch (stage) {
                    case WIZARD_FIRST_BOUND:
                        increment(event.getPlayer());
                        WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_FIRST_BOUND]
                                = new Location3D(c.getWorld().getName(), c.getX(), c.getY(), c.getZ());
                        event.getPlayer().sendMessage(INFO_COLOR + "[FlintSpleef] Okay! The first boundary corner "
                                + "will be at " + EM_COLOR + "(" + c.getX() + ", " + c.getY() + ", " + c.getZ() + ")"
                                + INFO_COLOR + ". Next, please click the block you would like to use as the second "
                                + "corner of the arena's boundary.");
                        break;
                    case WIZARD_SECOND_BOUND:
                        if (c.getWorld().getName().equals(
                                ((Location3D) WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_FIRST_BOUND])
                                        .getWorld().get()
                        )) {
                            increment(event.getPlayer());
                            WIZARD_INFO.get(event.getPlayer().getUniqueId())[WIZARD_SECOND_BOUND]
                                    = new Location3D(c.getWorld().getName(), c.getX(), c.getY(), c.getZ());
                            event.getPlayer().sendMessage(INFO_COLOR + "[FlintSpleef] Okay! The second boundary corner "
                                    + "will be at " + EM_COLOR + "(" + c.getX() + ", " + c.getY() + ", " + c.getZ()
                                    + ")" + INFO_COLOR + ". Next, please stand at the location you wish to use at the "
                                    + "spawn point for the arena and type " + EM_COLOR + "OK" + INFO_COLOR + ".");
                        } else {
                            event.getPlayer().sendMessage(ERROR_COLOR + "[FlintSpleef] The second boundary corner must "
                                    + "be in the same world as the first");
                        }
                        break;
                    default:
                        event.setCancelled(false);
                        break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.PLAYER) {
            if (Main.getMinigame().getChallenger(event.getEntity().getUniqueId()).isPresent()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (WIZARDS.containsKey(event.getPlayer().getUniqueId())) {
            WIZARDS.remove(event.getPlayer().getUniqueId());
            WIZARD_INFO.remove(event.getPlayer().getUniqueId());
        }
    }

    private void increment(Player player) {
        WIZARDS.put(player.getUniqueId(), WIZARDS.get(player.getUniqueId()) + 1);
    }

}
