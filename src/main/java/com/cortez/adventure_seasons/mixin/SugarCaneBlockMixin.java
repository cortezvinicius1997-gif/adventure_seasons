package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.season.CropGrowthManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!CropGrowthManager.shouldGrow(world, pos, state, random)) {
            ci.cancel();
        }
    }
}
