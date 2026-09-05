package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.AdventureSeason;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import com.cortez.adventure_seasons.lib.util.Meltable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceBlock.class)
public abstract class IceBlockMixin extends Block implements Meltable {

    public IceBlockMixin(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Shadow
    protected abstract void melt(BlockState state, Level world, BlockPos pos);

    @Inject(at = @At("HEAD"), method = "randomTick")
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (this == Blocks.ICE && world.getBrightness(LightLayer.SKY, pos) > 0 && world.getBiome(pos).value().getBaseTemperature() >= 0.15F) {
            if (!AdventureSeason.getPlacedMeltablesState(world).isManuallyPlaced(pos)) {
                this.melt(state, world, pos);
            } else if (AdventureSeasonConfig.isShouldIceNearWaterMelt()) {
                boolean nearWater = false;
                for (BlockPos nearPos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                    if (world.getFluidState(nearPos).getType().is(FluidTags.WATER)) {
                        nearWater = true;
                        break;
                    }
                }
                if (nearWater) {
                    this.melt(state, world, pos);
                }
            }
        }
    }

}
