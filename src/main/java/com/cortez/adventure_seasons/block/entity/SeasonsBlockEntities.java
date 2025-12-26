package com.cortez.adventure_seasons.block.entity;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.block.AdventureSeasonBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class SeasonsBlockEntities {

    public static final BlockEntityType<SeasonSensorEntity> SENSOR_ENTITY_BLOCK_ENTITY_TYPE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(AdventureSeasons.MODID, "season_sensor_be"),
                    BlockEntityType.Builder.create(SeasonSensorEntity::new, AdventureSeasonBlocks.SEASONSENSOR).build(null));

    public static final BlockEntityType<SeasonCalendarEntity> SEASON_CALENDAR_ENTITY_BLOCK_ENTITY_TYPE = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(AdventureSeasons.MODID, "season_calendar_be"),
            BlockEntityType.Builder.create(SeasonCalendarEntity::new, AdventureSeasonBlocks.SEASONCALENDAR).build(null));

    public static void registerBlockEntities() {
        AdventureSeasons.LOGGER.info("Registering Block Entities for " + AdventureSeasons.MODID);
    }
}
