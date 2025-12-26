package com.cortez.adventure_seasons.group;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.block.AdventureSeasonBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AdventureSeasonsGroup
{
    public static final ItemGroup ADVENTURE_MOD_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(AdventureSeasons.MODID, "adventure_seasons"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.adventure_season"))
                    .icon(()-> new ItemStack(AdventureSeasonBlocks.SEASONSENSOR)).entries(((displayContext, entries) -> {
                        entries.add(AdventureSeasonBlocks.SEASONSENSOR);
                        entries.add(AdventureSeasonBlocks.SEASONCALENDAR);
                    })).build());

    public static void registerItemGroups() {
        AdventureSeasons.LOGGER.info("Registering Item Groups for " + AdventureSeasons.MODID);
    }
}
