package com.cortez.adventure_seasons.datagen;

import com.cortez.adventure_seasons.block.AdventureSeasonBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class AdventureSeasonsRecipeProvider extends FabricRecipeProvider {
    public AdventureSeasonsRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output)
    {
        return new RecipeProvider(registries, output) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.MISC, AdventureSeasonBlocks.SEASONSENSOR)
                        .pattern("   ")
                        .pattern("#I#")
                        .pattern("###")
                        .define('#', Blocks.DEEPSLATE_BRICK_SLAB)
                        .define('I', Items.ITEM_FRAME)
                        .unlockedBy(getHasName(Blocks.DEEPSLATE_BRICK_SLAB), has(Blocks.DEEPSLATE_BRICK_SLAB))
                        .unlockedBy(getHasName(Items.ITEM_FRAME), has(Items.ITEM_FRAME))
                        .group("season_sensor")
                        .save(output);

                shaped(RecipeCategory.MISC, AdventureSeasonBlocks.SEASONCALENDAR, 3)
                        .pattern("PP ")
                        .pattern("II ")
                        .pattern("## ")
                        .define('#', Blocks.OAK_SLAB)
                        .define('I', Items.ITEM_FRAME)
                        .define('P', Items.PAPER)
                        .unlockedBy(getHasName(Blocks.OAK_SLAB), has(Blocks.OAK_SLAB))
                        .unlockedBy(getHasName(Items.ITEM_FRAME), has(Items.ITEM_FRAME))
                        .unlockedBy(getHasName(Items.PAPER), has(Items.PAPER))
                        .group("season_calendar")
                        .save(output);
            }
        };
    }

    @Override
    public String getName() {
        return "Adventure Seasons Recipes";
    }
}
