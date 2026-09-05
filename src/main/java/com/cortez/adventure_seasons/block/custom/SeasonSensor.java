package com.cortez.adventure_seasons.block.custom;

import com.cortez.adventure_seasons.block.custom.state.SeasonSensorState;
import com.cortez.adventure_seasons.block.entity.SeasonSensorEntity;
import com.cortez.adventure_seasons.block.entity.SeasonsBlockEntities;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SeasonSensor extends DaylightDetectorBlock {

    public static final EnumProperty<SeasonSensorState> SEASON =
            EnumProperty.create("season", SeasonSensorState.class);

    private static final VoxelShape SHAPE =
            Block.box(0, 0, 0, 16, 4, 16);

    public SeasonSensor(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.defaultBlockState()
                        .setValue(POWER, 0)
                        .setValue(SEASON, SeasonSensorState.SPRING)
        );
    }

    public static void updateState(BlockState state, Level level, BlockPos pos) {
        Season currentSeason = SeasonState.get();
        SeasonSensorState sensorState = state.getValue(SEASON);

        boolean matches =
                (sensorState == SeasonSensorState.SPRING && currentSeason == Season.SPRING) ||
                        (sensorState == SeasonSensorState.SUMMER && currentSeason == Season.SUMMER) ||
                        (sensorState == SeasonSensorState.AUTUMN && currentSeason == Season.AUTUMN) ||
                        (sensorState == SeasonSensorState.WINTER && currentSeason == Season.WINTER);

        int power = matches ? 15 : 0;
        level.setBlock(pos, state.setValue(POWER, power), 3);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!player.canUseGameMasterBlocks()) {
            return super.useWithoutItem(state, level, pos, player, hit);
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockState newState = state.cycle(SEASON);
        level.setBlock(pos, newState, 4);
        updateState(newState, level, pos);

        return InteractionResult.CONSUME;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SeasonSensorEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SEASON);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return !level.isClientSide()
                ? createTicker(SeasonSensorEntity::tick)
                : null;
    }

    @SuppressWarnings("unchecked")
    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTicker(BlockEntityTicker<? super E> ticker) {
        return (BlockEntityTicker<A>) ticker;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }
}
