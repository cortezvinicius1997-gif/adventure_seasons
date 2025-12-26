package com.cortez.adventure_seasons.block.entity;

import com.cortez.adventure_seasons.block.custom.SeasonSensor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SeasonSensorEntity extends BlockEntity
{

    public SeasonSensorEntity(BlockPos pos, BlockState state) {
        super(SeasonsBlockEntities.SENSOR_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, SeasonSensorEntity entity) {
        if (world.getTime() % 20L == 0L) {
            Block block = state.getBlock();
            if (block instanceof SeasonSensor) {
                SeasonSensor.updateState(state, world, pos);
            }
        }
    }
}
