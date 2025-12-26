package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.season.CropGrowthManager;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para abóboras e melancias (StemBlock)
 */
@Mixin(StemBlock.class)
class StemBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!CropGrowthManager.shouldGrow(world, pos, state, random)) {
            ci.cancel();
        }
    }
}

/**
 * Mixin para mudas de árvores (SaplingBlock)
 */
@Mixin(SaplingBlock.class)
class SaplingBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!CropGrowthManager.shouldGrow(world, pos, state, random)) {
            ci.cancel();
        }
    }
}

/**
 * Mixin para arbustos de sweet berries
 */
@Mixin(SweetBerryBushBlock.class)
class SweetBerryBushBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!CropGrowthManager.shouldGrow(world, pos, state, random)) {
            ci.cancel();
        }
    }
}

/**
 * Mixin para Nether Wart
 */
@Mixin(NetherWartBlock.class)
class NetherWartBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!CropGrowthManager.shouldGrow(world, pos, state, random)) {
            ci.cancel();
        }
    }
}

/**
 * Mixin para bambu
 */
@Mixin(BambooBlock.class)
class BambooBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!CropGrowthManager.shouldGrow(world, pos, state, random)) {
            ci.cancel();
        }
    }
}

/**
 * Mixin para cactos
 */
@Mixin(CactusBlock.class)
class CactusBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!CropGrowthManager.shouldGrow(world, pos, state, random)) {
            ci.cancel();
        }
    }
}

/**
 * Mixin para cana-de-açúcar
 */
@Mixin(SugarCaneBlock.class)
class SugarCaneBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!CropGrowthManager.shouldGrow(world, pos, state, random)) {
            ci.cancel();
        }
    }
}