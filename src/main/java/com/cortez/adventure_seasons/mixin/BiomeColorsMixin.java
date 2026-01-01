package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.mixed.BiomeMixed;
import com.cortez.adventure_seasons.lib.network.SeasonNetworkClient;
import com.cortez.adventure_seasons.lib.resources.FoliageSeasonColors;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class BiomeColorsMixin {

    // Noise sampler próprio para substituir TEMPERATURE_NOISE
    @Unique
    private static final SimplexNoiseSampler SEASON_NOISE = new SimplexNoiseSampler(new CheckedRandom(2345L));

    /**
     * Obtém a estação atual, usando a versão sincronizada do servidor em multiplayer
     */
    @Unique
    private static Season getCurrentSeason() {
        return SeasonNetworkClient.isInitialized()
                ? SeasonNetworkClient.getSeason()
                : SeasonState.get();
    }

    @SuppressWarnings({"ConstantValue", "removal"})
    @Inject(at = @At("RETURN"), method = "method_23791", cancellable = true)
    private static void enhanceFallColors(Biome biome, double x, double z, CallbackInfoReturnable<Integer> cir) {
        Season season = getCurrentSeason();
        if(season == Season.AUTUMN && ((Object) biome) instanceof BiomeMixed mixed && mixed.getOriginalWeather() != null) {
            double d = MathHelper.clamp(mixed.getOriginalWeather().temperature(), 0.0F, 1.0F);
            double e = MathHelper.clamp(mixed.getOriginalWeather().downfall(), 0.0F, 1.0F);
            int fallFoliageColor = FoliageSeasonColors.getColor(Season.SubSeason.EARLY_AUTUMN, d, e);
            if(cir.getReturnValue() == fallFoliageColor) {
                double sample = SEASON_NOISE.sample(x * 0.0225, z * 0.0225, 0.0);
                cir.setReturnValue(sample < 0.25 ? fallFoliageColor : FoliageSeasonColors.getColor(Season.SubSeason.EARLY_AUTUMN, 0.85, 0.9));
            }
        }
    }

}
