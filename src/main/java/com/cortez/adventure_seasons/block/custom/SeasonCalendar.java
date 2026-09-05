package com.cortez.adventure_seasons.block.custom;

import com.cortez.adventure_seasons.block.entity.SeasonCalendarEntity;
import com.cortez.adventure_seasons.block.entity.SeasonsBlockEntities;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class SeasonCalendar extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Season.SubSeason> SUBSEASON = EnumProperty.create("subseason", Season.SubSeason.class);

    public static final MapCodec<SeasonCalendar> CODEC = simpleCodec(SeasonCalendar::new);

    public static final VoxelShape SHAPE_S = Stream.of(
            Block.box(0, 3, 15, 16, 12, 16),
            Block.box(1.25, 11, 14.75, 1.85, 12.25, 15.9),
            Block.box(2.25, 11, 14.75, 2.85, 12.25, 15.9),
            Block.box(3.25, 11, 14.75, 3.85, 12.25, 15.9),
            Block.box(4.25, 11, 14.75, 4.85, 12.25, 15.9),
            Block.box(5.25, 11, 14.75, 5.85, 12.25, 15.9),
            Block.box(6.25, 11, 14.75, 6.85, 12.25, 15.9),
            Block.box(7.25, 11, 14.75, 7.85, 12.25, 15.9),
            Block.box(8.25, 11, 14.75, 8.85, 12.25, 15.9),
            Block.box(9.25, 11, 14.75, 9.85, 12.25, 15.9),
            Block.box(0.75, 3.75, 14.8, 10.25, 11.25, 15.3),
            Block.box(10.85, 5.75, 14.8, 15.35, 9.25, 15.3)
    ).reduce((v1, v2) -> Shapes.joinUnoptimized(v1, v2, BooleanOp.OR)).get();

    public static final VoxelShape SHAPE_N = Stream.of(
            Block.box(0, 3, 0, 16, 12, 1),
            Block.box(14.15, 11, 0.10000000000000142, 14.75, 12.25, 1.25),
            Block.box(13.15, 11, 0.10000000000000142, 13.75, 12.25, 1.25),
            Block.box(12.15, 11, 0.10000000000000142, 12.75, 12.25, 1.25),
            Block.box(11.15, 11, 0.10000000000000142, 11.75, 12.25, 1.25),
            Block.box(10.15, 11, 0.10000000000000142, 10.75, 12.25, 1.25),
            Block.box(9.15, 11, 0.10000000000000142, 9.75, 12.25, 1.25),
            Block.box(8.15, 11, 0.10000000000000142, 8.75, 12.25, 1.25),
            Block.box(7.15, 11, 0.10000000000000142, 7.75, 12.25, 1.25),
            Block.box(6.15, 11, 0.10000000000000142, 6.75, 12.25, 1.25),
            Block.box(5.75, 3.75, 0.6999999999999993, 15.25, 11.25, 1.1999999999999993),
            Block.box(0.6500000000000004, 5.75, 0.6999999999999993, 5.15, 9.25, 1.1999999999999993)
    ).reduce((v1, v2) -> Shapes.joinUnoptimized(v1, v2, BooleanOp.OR)).get();


    public static final VoxelShape SHAPE_W = Stream.of(
            Block.box(0, 3, 0, 1, 12, 16),
            Block.box(0.10000000000000142, 11, 1.25, 1.25, 12.25, 1.8499999999999996),
            Block.box(0.10000000000000142, 11, 2.25, 1.25, 12.25, 2.8499999999999996),
            Block.box(0.10000000000000142, 11, 3.25, 1.25, 12.25, 3.8499999999999996),
            Block.box(0.10000000000000142, 11, 4.25, 1.25, 12.25, 4.85),
            Block.box(0.10000000000000142, 11, 5.25, 1.25, 12.25, 5.85),
            Block.box(0.10000000000000142, 11, 6.25, 1.25, 12.25, 6.85),
            Block.box(0.10000000000000142, 11, 7.25, 1.25, 12.25, 7.85),
            Block.box(0.10000000000000142, 11, 8.25, 1.25, 12.25, 8.85),
            Block.box(0.10000000000000142, 11, 9.25, 1.25, 12.25, 9.85),
            Block.box(0.6999999999999993, 3.75, 0.75, 1.1999999999999993, 11.25, 10.25),
            Block.box(0.6999999999999993, 5.75, 10.85, 1.1999999999999993, 9.25, 15.350000000000001)
    ).reduce((v1, v2) -> Shapes.joinUnoptimized(v1, v2, BooleanOp.OR)).get();

    public static final VoxelShape SHAPE_E = Stream.of(
            Block.box(15, 3, 0, 16, 12, 16),
            Block.box(14.75, 11, 14.149999999999999, 15.899999999999999, 12.25, 14.75),
            Block.box(14.75, 11, 13.149999999999999, 15.899999999999999, 12.25, 13.75),
            Block.box(14.75, 11, 12.15, 15.899999999999999, 12.25, 12.75),
            Block.box(14.75, 11, 11.15, 15.899999999999999, 12.25, 11.75),
            Block.box(14.75, 11, 10.15, 15.899999999999999, 12.25, 10.75),
            Block.box(14.75, 11, 9.15, 15.899999999999999, 12.25, 9.75),
            Block.box(14.75, 11, 8.15, 15.899999999999999, 12.25, 8.75),
            Block.box(14.75, 11, 7.15, 15.899999999999999, 12.25, 7.75),
            Block.box(14.75, 11, 6.15, 15.899999999999999, 12.25, 6.75),
            Block.box(14.8, 3.75, 5.75, 15.3, 11.25, 15.25),
            Block.box(14.8, 5.75, 0.6499999999999986, 15.3, 9.25, 5.15)
    ).reduce((v1, v2) -> Shapes.joinUnoptimized(v1, v2, BooleanOp.OR)).get();

    public SeasonCalendar(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.defaultBlockState()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(SUBSEASON, Season.SubSeason.EARLY_SPRING)
        );
    }

    public static void updateState(BlockState state, Level level, BlockPos pos)
    {
        Season.SubSeason current = SeasonState.getSubSeason();
        level.setBlock(pos, state.setValue(SUBSEASON, current), Block.UPDATE_ALL);
    }


    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }


    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SeasonCalendarEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)){
            case NORTH -> SHAPE_N;
            case WEST -> SHAPE_W;
            case EAST -> SHAPE_E;
            case SOUTH -> SHAPE_S;
            default -> SHAPE_N;
        };
    }


    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Season.SubSeason current = SeasonState.getSubSeason();
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(SUBSEASON, current);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(SUBSEASON);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide() && type == SeasonsBlockEntities.SEASON_CALENDAR_ENTITY_BLOCK_ENTITY_TYPE
                ? createTicker(SeasonCalendarEntity::tick)
                : null;
    }

    @SuppressWarnings("unchecked")
    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTicker(BlockEntityTicker<? super E> ticker) {
        return (BlockEntityTicker<A>) ticker;
    }
}
