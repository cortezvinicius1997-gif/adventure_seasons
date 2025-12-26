package com.cortez.adventure_seasons.block.entity;

import com.cortez.adventure_seasons.block.custom.SeasonCalendar;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SeasonCalendarEntity extends BlockEntity {
    public SeasonCalendarEntity(BlockPos pos, BlockState state) {
        super(SeasonsBlockEntities.SEASON_CALENDAR_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, SeasonCalendarEntity entity) {
        if (world.getTime() % 20L == 0L) {
            Block block = state.getBlock();
            if (block instanceof SeasonCalendar) {
                SeasonCalendar.updateState(state, world, pos);
            }
        }
    }
}
