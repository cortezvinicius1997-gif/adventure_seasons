package com.cortez.adventure_seasons.util;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.block.AdventureSeasonBlocks;
import com.cortez.adventure_seasons.block.entity.SeasonsBlockEntities;
import com.cortez.adventure_seasons.commands.SeasonCommands;
import com.cortez.adventure_seasons.events.PlayerTickHandler;
import com.cortez.adventure_seasons.events.StartServer;
import com.cortez.adventure_seasons.group.AdventureSeasonsGroup;
import com.cortez.adventure_seasons.lib.AdventureSeason;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import com.cortez.adventure_seasons.lib.network.AdventureSeasonsNetwork;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class ModRegisterSeason {
    public static void register() {
        registerEvents();
        registerModConfig();
        registerCommands();
        registerBlocks();
        registerBlockEntity();
        registerGroup();
        networkRegister();
    }

    private static void registerBlocks() {
        AdventureSeasonBlocks.registerModBlocks();
    }

    private static void registerGroup() {
        AdventureSeasonsGroup.registerItemGroups();
    }

    private static void registerModConfig() {
        AdventureSeasonConfig.load();
    }

    private static void networkRegister() {
        AdventureSeasonsNetwork.registerC2SPackets();
        AdventureSeasonsNetwork.registerS2CPackets();
    }

    private static void registerBlockEntity() {
        SeasonsBlockEntities.registerBlockEntities();
    }

    private static void registerEvents() {
        AdventureSeasons.LOGGER.info("Started Seasons Mod");
        AdventureSeason adventureSeason = new AdventureSeason();
        AdventureSeason season = StartServer.register(adventureSeason);
        ServerTickEvents.START_SERVER_TICK.register(new PlayerTickHandler(season));
    }

    public static void registerCommands() {
        SeasonCommands.registerCommand();
    }
}