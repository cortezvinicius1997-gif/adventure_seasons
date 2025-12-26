package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.AdventureSeason;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import com.cortez.adventure_seasons.lib.util.Meltable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IceBlock;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IceBlock.class)
public abstract class IceBlockMixin extends Block implements Meltable {

    public IceBlockMixin(Settings settings) {
        super(settings);
    }

    @Shadow protected abstract void melt(BlockState state, World world, BlockPos pos);

    @Inject(at = @At("HEAD"), method = "randomTick")
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (this == Blocks.ICE && world.getLightLevel(LightType.SKY, pos) > 0 && world.getBiome(pos).value().getTemperature(pos) >= 0.15F) {
            if(!AdventureSeason.getPlacedMeltablesState(world).isManuallyPlaced(pos)) {
                this.melt(state, world, pos);
            }else if(AdventureSeasonConfig.isShouldIceNearWaterMelt()) {
                boolean nearWater = false;
                for(BlockPos nearPos : BlockPos.iterateOutwards(pos, 1, 1, 1)) {
                    if(world.getFluidState(nearPos).isIn(FluidTags.WATER)) {
                        nearWater = true;
                        break;
                    }
                }
                if(nearWater) {
                    this.melt(state, world, pos);
                }
            }
        }
    }

}
