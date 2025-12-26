package com.cortez.adventure_seasons.block;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.block.custom.SeasonCalendar;
import com.cortez.adventure_seasons.block.custom.SeasonSensor;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class AdventureSeasonBlocks
{
    public static final Block SEASONSENSOR = registerBlock("season_sensor", new SeasonSensor(AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)));
    public static final Block SEASONCALENDAR = registerBlock("season_calendar", new SeasonCalendar(AbstractBlock.Settings.copy(Blocks.OAK_PLANKS)));


    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(AdventureSeasons.MODID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(AdventureSeasons.MODID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        AdventureSeasons.LOGGER.info("Registering Mod Blocks for " + AdventureSeasons.MODID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
            entries.add(AdventureSeasonBlocks.SEASONSENSOR);
            entries.add(AdventureSeasonBlocks.SEASONCALENDAR);
        });
    }
}
