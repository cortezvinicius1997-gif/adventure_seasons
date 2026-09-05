package com.cortez.adventure_seasons.block.entity;

import com.cortez.adventure_seasons.block.custom.SeasonCalendar;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SeasonCalendarEntity extends BlockEntity {
    public SeasonCalendarEntity(BlockPos pos, BlockState state) {
        super(SeasonsBlockEntities.SEASON_CALENDAR_ENTITY_BLOCK_ENTITY_TYPE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SeasonCalendarEntity entity) {
        if (level.getGameTime() % 20L == 0L) {
            Block block = state.getBlock();
            if (block instanceof SeasonCalendar) {
                SeasonCalendar.updateState(state, level, pos);
            }
        }
    }
}
