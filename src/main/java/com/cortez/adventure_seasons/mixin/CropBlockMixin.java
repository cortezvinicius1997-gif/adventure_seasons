package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.season.CropGrowthManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin para modificar o crescimento de plantações baseado na estação.
 */
@Mixin(CropBlock.class)
public class CropBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        // Verifica se deve permitir o crescimento baseado na estação
        if (!CropGrowthManager.shouldGrow(world, pos, state, random)) {
            ci.cancel(); // Cancela o crescimento
        }
        // Se retornar true, o crescimento normal continua
    }
}