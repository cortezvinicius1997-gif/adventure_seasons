package com.cortez.adventure_seasons.lib.util;

import com.cortez.adventure_seasons.lib.AdventureSeason;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;

public interface Meltable
{
    TagKey<Block> REPLACEABLE_BY_SNOW = TagKey.of(RegistryKeys.BLOCK, AdventureSeason.identifier("replaceable_by_snow"));

    default void onMeltableReplaced(ServerWorld world, BlockPos pos) {
        AdventureSeason.getPlacedMeltablesState(world).setManuallyPlaced(pos, false);
        AdventureSeason.getReplacedMeltablesState(world).setReplaced(pos, null);
    }

    default void onMeltableManuallyPlaced(ServerWorld world, BlockPos pos) {
        AdventureSeason.getPlacedMeltablesState(world).setManuallyPlaced(pos, true);
    }

    static void replaceBlockOnSnow(ServerWorld world, BlockPos blockPos, Biome biome) {
        BlockState plantState = world.getBlockState(blockPos);
        if(plantState.isIn(REPLACEABLE_BY_SNOW)) {
            if (!biome.doesNotSnow(blockPos) && blockPos.getY() >= world.getBottomY() && blockPos.getY() < world.getTopY() && world.getLightLevel(LightType.BLOCK, blockPos) < 10) {
                BlockState upperState = world.getBlockState(blockPos.up());
                if(plantState.getProperties().contains(TallPlantBlock.HALF) && upperState.getProperties().contains(TallPlantBlock.HALF)) {
                    if(upperState.get(TallPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
                        AdventureSeason.setMeltable(blockPos);
                        AdventureSeason.getReplacedMeltablesState(world).setReplaced(blockPos, plantState);
                        world.setBlockState(blockPos, Blocks.SNOW.getDefaultState(), Block.FORCE_STATE);
                        world.setBlockState(blockPos.up(), Blocks.AIR.getDefaultState());
                        Blocks.SNOW.getDefaultState().updateNeighbors(world, blockPos, Block.NOTIFY_ALL);
                        world.updateListeners(blockPos, plantState, Blocks.SNOW.getDefaultState(), Block.NOTIFY_ALL);
                    }
                }else if(upperState.isAir()) {
                    AdventureSeason.setMeltable(blockPos);
                    AdventureSeason.getReplacedMeltablesState(world).setReplaced(blockPos, plantState);
                    world.setBlockState(blockPos, Blocks.SNOW.getDefaultState());
                }
            }
        }
    }

}
