package com.cortez.adventure_seasons.block.custom;

import com.cortez.adventure_seasons.block.entity.SeasonCalendarEntity;
import com.cortez.adventure_seasons.block.entity.SeasonSensorEntity;
import com.cortez.adventure_seasons.block.entity.SeasonsBlockEntities;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class SeasonCalendar extends BlockWithEntity {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<Season.SubSeason> SUBSEASON =  EnumProperty.of("subseason", Season.SubSeason.class);

    public static final MapCodec<SeasonCalendar> CODEC = SeasonCalendar.createCodec(SeasonCalendar::new);

    public static final VoxelShape SHAPE_S = Stream.of(
            Block.createCuboidShape(0, 3, 15, 16, 12, 16),
            Block.createCuboidShape(1.25, 11, 14.75, 1.85, 12.25, 15.9),
            Block.createCuboidShape(2.25, 11, 14.75, 2.85, 12.25, 15.9),
            Block.createCuboidShape(3.25, 11, 14.75, 3.85, 12.25, 15.9),
            Block.createCuboidShape(4.25, 11, 14.75, 4.85, 12.25, 15.9),
            Block.createCuboidShape(5.25, 11, 14.75, 5.85, 12.25, 15.9),
            Block.createCuboidShape(6.25, 11, 14.75, 6.85, 12.25, 15.9),
            Block.createCuboidShape(7.25, 11, 14.75, 7.85, 12.25, 15.9),
            Block.createCuboidShape(8.25, 11, 14.75, 8.85, 12.25, 15.9),
            Block.createCuboidShape(9.25, 11, 14.75, 9.85, 12.25, 15.9),
            Block.createCuboidShape(0.75, 3.75, 14.8, 10.25, 11.25, 15.3),
            Block.createCuboidShape(10.85, 5.75, 14.8, 15.35, 9.25, 15.3)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    public static final VoxelShape SHAPE_N = Stream.of(
            Block.createCuboidShape(0, 3, 0, 16, 12, 1),
            Block.createCuboidShape(14.15, 11, 0.10000000000000142, 14.75, 12.25, 1.25),
            Block.createCuboidShape(13.15, 11, 0.10000000000000142, 13.75, 12.25, 1.25),
            Block.createCuboidShape(12.15, 11, 0.10000000000000142, 12.75, 12.25, 1.25),
            Block.createCuboidShape(11.15, 11, 0.10000000000000142, 11.75, 12.25, 1.25),
            Block.createCuboidShape(10.15, 11, 0.10000000000000142, 10.75, 12.25, 1.25),
            Block.createCuboidShape(9.15, 11, 0.10000000000000142, 9.75, 12.25, 1.25),
            Block.createCuboidShape(8.15, 11, 0.10000000000000142, 8.75, 12.25, 1.25),
            Block.createCuboidShape(7.15, 11, 0.10000000000000142, 7.75, 12.25, 1.25),
            Block.createCuboidShape(6.15, 11, 0.10000000000000142, 6.75, 12.25, 1.25),
            Block.createCuboidShape(5.75, 3.75, 0.6999999999999993, 15.25, 11.25, 1.1999999999999993),
            Block.createCuboidShape(0.6500000000000004, 5.75, 0.6999999999999993, 5.15, 9.25, 1.1999999999999993)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();


    public static final VoxelShape SHAPE_W = Stream.of(
            Block.createCuboidShape(0, 3, 0, 1, 12, 16),
            Block.createCuboidShape(0.10000000000000142, 11, 1.25, 1.25, 12.25, 1.8499999999999996),
            Block.createCuboidShape(0.10000000000000142, 11, 2.25, 1.25, 12.25, 2.8499999999999996),
            Block.createCuboidShape(0.10000000000000142, 11, 3.25, 1.25, 12.25, 3.8499999999999996),
            Block.createCuboidShape(0.10000000000000142, 11, 4.25, 1.25, 12.25, 4.85),
            Block.createCuboidShape(0.10000000000000142, 11, 5.25, 1.25, 12.25, 5.85),
            Block.createCuboidShape(0.10000000000000142, 11, 6.25, 1.25, 12.25, 6.85),
            Block.createCuboidShape(0.10000000000000142, 11, 7.25, 1.25, 12.25, 7.85),
            Block.createCuboidShape(0.10000000000000142, 11, 8.25, 1.25, 12.25, 8.85),
            Block.createCuboidShape(0.10000000000000142, 11, 9.25, 1.25, 12.25, 9.85),
            Block.createCuboidShape(0.6999999999999993, 3.75, 0.75, 1.1999999999999993, 11.25, 10.25),
            Block.createCuboidShape(0.6999999999999993, 5.75, 10.85, 1.1999999999999993, 9.25, 15.350000000000001)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    public static final VoxelShape SHAPE_E = Stream.of(
            Block.createCuboidShape(15, 3, 0, 16, 12, 16),
            Block.createCuboidShape(14.75, 11, 14.149999999999999, 15.899999999999999, 12.25, 14.75),
            Block.createCuboidShape(14.75, 11, 13.149999999999999, 15.899999999999999, 12.25, 13.75),
            Block.createCuboidShape(14.75, 11, 12.15, 15.899999999999999, 12.25, 12.75),
            Block.createCuboidShape(14.75, 11, 11.15, 15.899999999999999, 12.25, 11.75),
            Block.createCuboidShape(14.75, 11, 10.15, 15.899999999999999, 12.25, 10.75),
            Block.createCuboidShape(14.75, 11, 9.15, 15.899999999999999, 12.25, 9.75),
            Block.createCuboidShape(14.75, 11, 8.15, 15.899999999999999, 12.25, 8.75),
            Block.createCuboidShape(14.75, 11, 7.15, 15.899999999999999, 12.25, 7.75),
            Block.createCuboidShape(14.75, 11, 6.15, 15.899999999999999, 12.25, 6.75),
            Block.createCuboidShape(14.8, 3.75, 5.75, 15.3, 11.25, 15.25),
            Block.createCuboidShape(14.8, 5.75, 0.6499999999999986, 15.3, 9.25, 5.15)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    public SeasonCalendar(Settings settings) {
        super(settings);
        this.setDefaultState(
                this.stateManager.getDefaultState()
                        .with(FACING, Direction.NORTH)
                        .with(SUBSEASON, Season.SubSeason.EARLY_SPRING)
        );
    }

    public static void updateState(BlockState state, World world, BlockPos pos)
    {
        Season.SubSeason current = SeasonState.getSubSeason();
        world.setBlockState(pos, state.with(SUBSEASON, current));
    }


    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
        super.appendTooltip(stack, context, tooltip, options);
        tooltip.add(Text.literal("teste"));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }


    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SeasonCalendarEntity(pos, state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)){
            case NORTH -> SHAPE_N;
            case WEST -> SHAPE_W;
            case EAST -> SHAPE_E;
            case SOUTH -> SHAPE_S;
            default -> SHAPE_N;
        };
    }


    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        Season.SubSeason current = SeasonState.getSubSeason();
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing()).with(SUBSEASON, current);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(SUBSEASON);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world,BlockState state,  BlockEntityType<T> type) {
        return world.isClient ? null : validateTicker(type, SeasonsBlockEntities.SEASON_CALENDAR_ENTITY_BLOCK_ENTITY_TYPE, SeasonCalendarEntity::tick);
    }
}
