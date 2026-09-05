package com.cortez.adventure_seasons.lib.util;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.lib.AdventureSeason;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.resources.Identifier;

public interface Meltable
{
    TagKey<Block> REPLACEABLE_BY_SNOW = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AdventureSeasons.MODID, "replaceable_by_snow"));

    default void onMeltableReplaced(ServerLevel world, BlockPos pos) {
        AdventureSeason.getPlacedMeltablesState(world).setManuallyPlaced(pos, false);
        AdventureSeason.getReplacedMeltablesState(world).setReplaced(pos, null);
    }

    default void onMeltableManuallyPlaced(ServerLevel world, BlockPos pos) {
        AdventureSeason.getPlacedMeltablesState(world).setManuallyPlaced(pos, true);
    }

    static void replaceBlockOnSnow(ServerLevel world, BlockPos blockPos, Biome biome) {
        BlockState plantState = world.getBlockState(blockPos);
        if(plantState.is(REPLACEABLE_BY_SNOW)) {
            if (biome.coldEnoughToSnow(blockPos, world.getSeaLevel()) && blockPos.getY() >= world.getMinY() && blockPos.getY() < world.getMaxY() && world.getBrightness(LightLayer.BLOCK, blockPos) < 10) {
                BlockState upperState = world.getBlockState(blockPos.above());
                if(plantState.getProperties().contains(DoublePlantBlock.HALF) && upperState.getProperties().contains(DoublePlantBlock.HALF)) {
                    if(upperState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                        AdventureSeason.setMeltable(blockPos);
                        AdventureSeason.getReplacedMeltablesState(world).setReplaced(blockPos, plantState);
                        world.setBlock(blockPos, Blocks.SNOW.defaultBlockState(), Block.UPDATE_ALL);
                        world.setBlock(blockPos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }else if(upperState.isAir()) {
                    AdventureSeason.setMeltable(blockPos);
                    AdventureSeason.getReplacedMeltablesState(world).setReplaced(blockPos, plantState);
                    world.setBlockAndUpdate(blockPos, Blocks.SNOW.defaultBlockState());
                }
            }
        }
    }

}
