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
package net.caseif.flint.spleef;

import net.caseif.flint.FlintCore;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.minigame.Minigame;
import net.caseif.flint.round.LifecycleStage;
import net.caseif.flint.spleef.command.CommandHandler;
import net.caseif.flint.spleef.command.CreateArenaCommand;
import net.caseif.flint.spleef.listener.BlockListener;
import net.caseif.flint.spleef.listener.MinigameListener;
import net.caseif.flint.spleef.listener.PlayerListener;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Main extends JavaPlugin {

    private static final int MIN_FLINT_VERSION = 1;

    public static final String WAITING_STAGE_ID = "waiting";
    public static final String PREPARING_STAGE_ID = "preparing";
    public static final String PLAYING_STAGE_ID = "playing";

    public static final String PREFIX = ChatColor.GREEN + "[FlintSpleef] ";
    public static final ChatColor INFO_COLOR = ChatColor.DARK_AQUA;
    public static final ChatColor ERROR_COLOR = ChatColor.RED;
    public static final ChatColor EM_COLOR = ChatColor.GOLD;

    public static ArrayList<Material> SHOVELS = new ArrayList<>();
    public static ItemStack SHOVEL;

    private static JavaPlugin plugin;
    private static Minigame mg;

    public static int MIN_PLAYERS = Integer.MAX_VALUE;

    static {
        SHOVELS.add(Material.WOOD_SPADE);
        SHOVELS.add(Material.STONE_SPADE);
        SHOVELS.add(Material.IRON_SPADE);
        SHOVELS.add(Material.GOLD_SPADE);
        SHOVELS.add(Material.DIAMOND_SPADE);
    }

    @Override
    public void onEnable() {
        if (FlintCore.getApiRevision() < MIN_FLINT_VERSION) {
            getLogger().severe("Flint API revision " + MIN_FLINT_VERSION
                    + " or greater is required for FlintSpleef to run! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // general plugin initialization
        plugin = this;
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        getCommand("fs").setExecutor(new CommandHandler());

        MIN_PLAYERS = getConfig().getInt("min-prep-players");

        int shovelType = getConfig().getInt("shovel-type");
        Material shovelMaterial = shovelType >= 0 && shovelType < SHOVELS.size()
                ? SHOVELS.get(shovelType)
                : SHOVELS.get(3);
        SHOVEL = new ItemStack(shovelMaterial, 1);

        // Flint initialization
        mg = FlintCore.registerPlugin(this.getName());
        mg.getEventBus().register(new MinigameListener());

        ImmutableSet<LifecycleStage> stages = ImmutableSet.copyOf(new LifecycleStage[]{
                new LifecycleStage(WAITING_STAGE_ID, -1),
                new LifecycleStage(PREPARING_STAGE_ID, getConfig().getInt("prep-time")),
                new LifecycleStage(PLAYING_STAGE_ID, getConfig().getInt("round-time"))
        });
        mg.setConfigValue(ConfigNode.DEFAULT_LIFECYCLE_STAGES, stages);
        mg.setConfigValue(ConfigNode.MAX_PLAYERS, getConfig().getInt("arena-size"));
    }

    @Override
    public void onDisable() {
        CreateArenaCommand.WIZARDS = null;
        CreateArenaCommand.WIZARD_INFO = null;
        SHOVELS = null;
        SHOVEL = null;
        mg = null;
        plugin = null;
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

    public static Minigame getMinigame() {
        return mg;
    }

}
