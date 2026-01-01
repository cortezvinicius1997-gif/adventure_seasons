package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.cache.ColorsCache;
import com.cortez.adventure_seasons.lib.mixed.BiomeMixed;
import com.cortez.adventure_seasons.lib.network.SeasonNetworkClient;
import com.cortez.adventure_seasons.lib.resources.FoliageSeasonColors;
import com.cortez.adventure_seasons.lib.resources.GrassSeasonColors;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Biome.class)
public abstract class BiomeClientMixin {

    @Shadow @Final private Biome.Weather weather;
    @Shadow @Final private BiomeEffects effects;

    @Shadow protected abstract int getDefaultGrassColor();
    @Shadow protected abstract int getDefaultFoliageColor();

    // Noise sampler próprio para substituir TEMPERATURE_NOISE
    @Unique
    private static final SimplexNoiseSampler SEASON_NOISE = new SimplexNoiseSampler(new CheckedRandom(2345L));

    /**
     * Obtém a subestação atual, usando a versão sincronizada do servidor em multiplayer
     */
    @Unique
    private Season.SubSeason getCurrentSubSeason() {
        return SeasonNetworkClient.isInitialized()
                ? SeasonNetworkClient.getSubSeason()
                : SeasonState.getSubSeason();
    }

    @Inject(at = @At("TAIL"), method = "getGrassColorAt", cancellable = true)
    public void getSeasonGrassColor(double x, double z, CallbackInfoReturnable<Integer> cir) {
        Biome biome = (Biome) ((Object) this);
        Optional<Integer> overridedColor;
        Season.SubSeason subSeason = getCurrentSubSeason();

        if(ColorsCache.hasGrassCache(biome)) {
            overridedColor = ColorsCache.getGrassCache(biome);
        }else {
            overridedColor = effects.getGrassColor();
            World world = MinecraftClient.getInstance().world;
            if(world != null) {
                Identifier biomeIdentifier = world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome);
                Optional<Integer> seasonGrassColor = GrassSeasonColors.getSeasonGrassColor(biome, biomeIdentifier, subSeason);
                if(seasonGrassColor.isPresent()) {
                    overridedColor = seasonGrassColor;
                }
            }
            ColorsCache.createGrassCache(biome, overridedColor);
        }
        if(effects.getGrassColorModifier() == BiomeEffects.GrassColorModifier.SWAMP) {
            int swampColor1 = GrassSeasonColors.getSwampColor1(subSeason);
            int swampColor2 = GrassSeasonColors.getSwampColor2(subSeason);

            double d = SEASON_NOISE.sample(x * 0.0225D, z * 0.0225D, 0.0D);
            cir.setReturnValue(d < -0.1D ? swampColor1 : swampColor2);
        }else if(overridedColor != null){
            Integer integer = overridedColor.orElseGet(this::getDefaultGrassColor);
            cir.setReturnValue(effects.getGrassColorModifier().getModifiedGrassColor(x, z, integer));
        }
    }

    @Inject(at = @At("TAIL"), method = "getFoliageColor", cancellable = true)
    public void getSeasonFoliageColor(CallbackInfoReturnable<Integer> cir) {
        Biome biome = (Biome) ((Object) this);
        Optional<Integer> overridedColor;
        Season.SubSeason subSeason = getCurrentSubSeason();

        if(ColorsCache.hasFoliageCache(biome)) {
            overridedColor = ColorsCache.getFoliageCache(biome);
        }else{
            overridedColor = effects.getFoliageColor();
            World world = MinecraftClient.getInstance().world;
            if(world != null) {
                Identifier biomeIdentifier = world.getRegistryManager().get(RegistryKeys.BIOME).getId(biome);
                Optional<Integer> seasonFoliageColor = FoliageSeasonColors.getSeasonFoliageColor(biome, biomeIdentifier, subSeason);
                if(seasonFoliageColor.isPresent()) {
                    overridedColor = seasonFoliageColor;
                }
            }
            ColorsCache.createFoliageCache(biome, overridedColor);
        }
        if(overridedColor != null) {
            Integer integer = overridedColor.orElseGet(this::getDefaultFoliageColor);
            cir.setReturnValue(integer);
        }
    }

    @Inject(at = @At("HEAD"), method = "getDefaultFoliageColor", cancellable = true)
    public void getSeasonDefaultFolliageColor(CallbackInfoReturnable<Integer> cir) {
        BiomeMixed mixed = (BiomeMixed) (Object) this;
        Biome.Weather originalWeather = mixed.getOriginalWeather();
        Season.SubSeason subSeason = getCurrentSubSeason();

        if(originalWeather != null) {
            double originalTemperature = MathHelper.clamp(originalWeather.temperature(), 0.0F, 1.0F);
            double originalDownfall = MathHelper.clamp(originalWeather.downfall(), 0.0F, 1.0F);
            cir.setReturnValue(FoliageSeasonColors.getColor(subSeason, originalTemperature, originalDownfall));
        }else{
            double temperature = MathHelper.clamp(this.weather.temperature(), 0.0F, 1.0F);
            double downfall = MathHelper.clamp(this.weather.downfall(), 0.0F, 1.0F);
            cir.setReturnValue(FoliageSeasonColors.getColor(subSeason, temperature, downfall));
        }
    }

    @Inject(at = @At("HEAD"), method = "getDefaultGrassColor", cancellable = true)
    public void getSeasonDefaultGrassColor(CallbackInfoReturnable<Integer> cir) {
        BiomeMixed mixed = (BiomeMixed) (Object) this;
        Biome.Weather originalWeather = mixed.getOriginalWeather();
        Season.SubSeason subSeason = getCurrentSubSeason();

        if(originalWeather != null) {
            double d = MathHelper.clamp(originalWeather.temperature(), 0.0F, 1.0F);
            double e = MathHelper.clamp(originalWeather.downfall(), 0.0F, 1.0F);
            cir.setReturnValue(GrassSeasonColors.getColor(subSeason, d, e));
        }else{
            double d = MathHelper.clamp(this.weather.temperature(), 0.0F, 1.0F);
            double e = MathHelper.clamp(this.weather.downfall(), 0.0F, 1.0F);
            cir.setReturnValue(GrassSeasonColors.getColor(subSeason, d, e));
        }
    }
}