package com.cortez.adventure_seasons.block.entity;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.block.AdventureSeasonBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

public class SeasonsBlockEntities {

    public static final BlockEntityType<SeasonSensorEntity> SENSOR_ENTITY_BLOCK_ENTITY_TYPE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(AdventureSeasons.MODID, "season_sensor_be"),
                    new BlockEntityType<>(SeasonSensorEntity::new, Set.of(AdventureSeasonBlocks.SEASONSENSOR)));

    public static final BlockEntityType<SeasonCalendarEntity> SEASON_CALENDAR_ENTITY_BLOCK_ENTITY_TYPE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(AdventureSeasons.MODID, "season_calendar_be"),
            new BlockEntityType<>(SeasonCalendarEntity::new, Set.of(AdventureSeasonBlocks.SEASONCALENDAR)));

    public static void registerBlockEntities() {
        AdventureSeasons.LOGGER.info("Registering Block Entities for " + AdventureSeasons.MODID);
    }
}
