package com.cortez.adventure_seasons.datagen;

import com.cortez.adventure_seasons.block.AdventureSeasonBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class AdventureSeasonsTableProvider extends FabricBlockLootSubProvider
{
    public AdventureSeasonsTableProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(packOutput, registriesFuture);
    }

    @Override
    public void generate() {
        dropSelf(AdventureSeasonBlocks.SEASONSENSOR);
        dropSelf(AdventureSeasonBlocks.SEASONCALENDAR);
    }


}
