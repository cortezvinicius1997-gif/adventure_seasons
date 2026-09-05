package com.cortez.adventure_seasons.mixin;


import com.cortez.adventure_seasons.lib.network.SeasonNetworkClient;
import com.cortez.adventure_seasons.lib.resources.FoliageSeasonColors;
import com.cortez.adventure_seasons.lib.resources.GrassSeasonColors;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Em 26.2 a coloração de blocos (grama/folhagem) migrou de BlockColors para
// BiomeColors. Este mixin aplica as cores sazonais às cores de bloco da grama e folhagem.
@Mixin(BiomeColors.class)
public class BlockColorsMixin {

    /**
     * Obtém a subestação atual, usando a versão sincronizada do servidor em multiplayer
     */
    private static Season.SubSeason getCurrentSubSeason() {
        return SeasonNetworkClient.isInitialized()
                ? SeasonNetworkClient.getSubSeason()
                : SeasonState.getSubSeason();
    }

    @Inject(method = "getAverageGrassColor", at = @At("HEAD"), cancellable = true)
    private static void injectGrassColor(BlockAndTintGetter world, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        int color = GrassSeasonColors.getColor(getCurrentSubSeason(), 0.5D, 1.0D);
        info.setReturnValue(0xFF000000 | (color & 0xFFFFFF));
    }

    @Inject(method = "getAverageFoliageColor", at = @At("HEAD"), cancellable = true)
    private static void injectFoliageColor(BlockAndTintGetter world, BlockPos pos, CallbackInfoReturnable<Integer> info) {
        int color = FoliageSeasonColors.getDefaultColor(getCurrentSubSeason());
        info.setReturnValue(0xFF000000 | (color & 0xFFFFFF));
    }
}