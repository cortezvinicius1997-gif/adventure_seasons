package com.cortez.adventure_seasons.mixin;


import com.cortez.adventure_seasons.lib.network.SeasonNetworkClient;
import com.cortez.adventure_seasons.lib.resources.FoliageSeasonColors;
import com.cortez.adventure_seasons.lib.resources.GrassSeasonColors;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
public class BlockColorsMixin {

    /**
     * Obtém a subestação atual, usando a versão sincronizada do servidor em multiplayer
     */
    private static Season.SubSeason getCurrentSubSeason() {
        return SeasonNetworkClient.isInitialized()
                ? SeasonNetworkClient.getSubSeason()
                : SeasonState.getSubSeason();
    }

    @Inject(method = "method_1693", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/GrassColors;getDefaultColor()I"), cancellable = true)
    private static void injectGrassColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(GrassSeasonColors.getColor(getCurrentSubSeason(), 0.5D, 1.0D));
        //AdventureMod.LOGGER.info("Color: " + GrassSeasonColors.getColor(SeasonState.get(), 0.5D, 1.0D));
    }

    @Inject(method = "method_1695", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/FoliageColors;getSpruceColor()I"), cancellable = true)
    private static void injectSpruceColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(FoliageSeasonColors.getSpruceColor(getCurrentSubSeason()));
    }

    @Inject(method = "method_1687", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/FoliageColors;getBirchColor()I"), cancellable = true)
    private static void injectBirchColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(FoliageSeasonColors.getBirchColor(getCurrentSubSeason()));
    }

    @Inject(method = "method_1692", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/FoliageColors;getDefaultColor()I"), cancellable = true)
    private static void injectFoliageColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(FoliageSeasonColors.getDefaultColor(getCurrentSubSeason()));
    }
}
