package com.cortez.adventure_seasons.group;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.block.AdventureSeasonBlocks;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class AdventureSeasonsGroup
{
    public static final ResourceKey<CreativeModeTab> ADVENTURE_MOD_GROUP_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath(AdventureSeasons.MODID, "adventure_seasons"));

    public static final CreativeModeTab ADVENTURE_MOD_GROUP = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB, ADVENTURE_MOD_GROUP_KEY,
            FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemgroup.adventure_season"))
                    .icon(() -> new ItemStack(AdventureSeasonBlocks.SEASONSENSOR))
                    .displayItems((displayContext, entries) -> {
                        entries.accept(AdventureSeasonBlocks.SEASONSENSOR);
                        entries.accept(AdventureSeasonBlocks.SEASONCALENDAR);
                    })
                    .build());

    public static void registerItemGroups() {
        AdventureSeasons.LOGGER.info("Registering Item Groups for " + AdventureSeasons.MODID);
    }
}
