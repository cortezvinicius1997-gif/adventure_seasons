package com.cortez.adventure_seasons.datagen;

import com.cortez.adventure_seasons.block.AdventureSeasonBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;

public class AdventureSeasonsBlockTagsProvider extends FabricTagsProvider.BlockTagsProvider
{
    public AdventureSeasonsBlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(AdventureSeasonBlocks.getRK(AdventureSeasonBlocks.SEASONSENSOR));

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(AdventureSeasonBlocks.getRK(AdventureSeasonBlocks.SEASONSENSOR));
    }
}
