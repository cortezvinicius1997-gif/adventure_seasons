package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.mixed.BiomeMixed;
import com.cortez.adventure_seasons.lib.network.SeasonNetworkClient;
import com.cortez.adventure_seasons.lib.resources.FoliageSeasonColors;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class BiomeColorsMixin {

    // Noise sampler próprio para substituir TEMPERATURE_NOISE
    @Unique
    private static final SimplexNoise SEASON_NOISE = new SimplexNoise(RandomSource.create(2345L));

    /**
     * Obtém a estação atual, usando a versão sincronizada do servidor em multiplayer
     */
    @Unique
    private static Season getCurrentSeason() {
        return SeasonNetworkClient.isInitialized()
                ? SeasonNetworkClient.getSeason()
                : SeasonState.get();
    }

    // O resolver de cor da folhagem (BiomeColors.FOLIAGE_COLOR_RESOLVER) é o análogo direto
    // do antigo method_23791. Recebe (Biome, x, z) e devolve a cor da folhagem do bioma.
    @SuppressWarnings({"ConstantValue", "removal"})
    @Inject(at = @At("RETURN"), method = "lambda$static$1", cancellable = true)
    private static void enhanceFallColors(Biome biome, double x, double z, CallbackInfoReturnable<Integer> cir) {
        Season season = getCurrentSeason();
        if(season == Season.AUTUMN && ((Object) biome) instanceof BiomeMixed mixed && mixed.getOriginalTemperatureModifier() != null) {
            double d = Mth.clamp(mixed.getOriginalTemperature(), 0.0F, 1.0F);
            double e = Mth.clamp(mixed.getOriginalDownfall(), 0.0F, 1.0F);
            int fallFoliageColor = FoliageSeasonColors.getColor(Season.SubSeason.EARLY_AUTUMN, d, e);
            if(cir.getReturnValue() == fallFoliageColor) {
                double sample = SEASON_NOISE.getValue(x * 0.0225, z * 0.0225, 0.0);
                cir.setReturnValue(sample < 0.25 ? fallFoliageColor : FoliageSeasonColors.getColor(Season.SubSeason.EARLY_AUTUMN, 0.85, 0.9));
            }
        }
    }

}
