package com.cortez.adventure_seasons.block.custom;

import com.cortez.adventure_seasons.block.custom.state.SeasonSensorState;
import com.cortez.adventure_seasons.block.entity.SeasonSensorEntity;
import com.cortez.adventure_seasons.block.entity.SeasonsBlockEntities;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SeasonSensor extends DaylightDetectorBlock {

    public static final EnumProperty<SeasonSensorState> SEASON =
            EnumProperty.of("season", SeasonSensorState.class);

    private static final VoxelShape SHAPE =
            Block.createCuboidShape(0, 0, 0, 16, 4, 16);

    public SeasonSensor(Settings settings) {
        super(settings);
        this.setDefaultState(
                this.stateManager.getDefaultState()
                        .with(POWER, 0)
                        .with(SEASON, SeasonSensorState.SPRING)
        );
    }

    public static void updateState(BlockState state, World world, BlockPos pos) {
        Season currentSeason = SeasonState.get();
        SeasonSensorState sensorState = state.get(SEASON);

        boolean matches =
                (sensorState == SeasonSensorState.SPRING && currentSeason == Season.SPRING) ||
                        (sensorState == SeasonSensorState.SUMMER && currentSeason == Season.SUMMER) ||
                        (sensorState == SeasonSensorState.AUTUMN && currentSeason == Season.AUTUMN) ||
                        (sensorState == SeasonSensorState.WINTER && currentSeason == Season.WINTER);

        int power = matches ? 15 : 0;
        world.setBlockState(pos, state.with(POWER, power), 3);
    }

    @Override
    protected ActionResult onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            BlockHitResult hit
    ) {
        if (!player.canModifyBlocks()) {
            return super.onUse(state, world, pos, player, hit);
        }

        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockState newState = state.cycle(SEASON);
        world.setBlockState(pos, newState, 4);
        updateState(newState, world, pos);

        return ActionResult.CONSUME;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SeasonSensorEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(SEASON);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return world.isClient
                ? null
                : validateTicker(
                type,
                SeasonsBlockEntities.SENSOR_ENTITY_BLOCK_ENTITY_TYPE,
                SeasonSensorEntity::tick
        );
    }

    @Override
    protected VoxelShape getOutlineShape(
            BlockState state,
            BlockView world,
            BlockPos pos,
            ShapeContext context
    ) {
        return SHAPE;
    }
}
