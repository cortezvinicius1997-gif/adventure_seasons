package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.cache.ColorsCache;
import com.cortez.adventure_seasons.lib.mixed.BiomeMixed;
import com.cortez.adventure_seasons.lib.network.SeasonNetworkClient;
import com.cortez.adventure_seasons.lib.resources.FoliageSeasonColors;
import com.cortez.adventure_seasons.lib.resources.GrassSeasonColors;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
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

    @Shadow @Final private BiomeSpecialEffects specialEffects;

    @Shadow public abstract int getBaseGrassColor();
    @Shadow public abstract int getFoliageColorFromTexture();

    // Noise sampler próprio para substituir TEMPERATURE_NOISE
    @Unique
    private static final SimplexNoise SEASON_NOISE = new SimplexNoise(RandomSource.create(2345L));

    /**
     * Obtém a subestação atual, usando a versão sincronizada do servidor em multiplayer
     */
    @Unique
    private Season.SubSeason getCurrentSubSeason() {
        return SeasonNetworkClient.isInitialized()
                ? SeasonNetworkClient.getSubSeason()
                : SeasonState.getSubSeason();
    }

    /**
     * Temperatura original (não modificada pela estação) via accessor do ClimateSettings
     */
    @Unique
    private float getOriginalClimateTemperature() {
        Object climateSettings = BiomeAccessor.getClimateSettings((Biome) (Object) this);
        return ((BiomeWeatherAccessor) (Object) climateSettings).getTemperature();
    }

    /**
     * Downfall original (não modificado pela estação) via accessor do ClimateSettings
     */
    @Unique
    private float getOriginalClimateDownfall() {
        Object climateSettings = BiomeAccessor.getClimateSettings((Biome) (Object) this);
        return ((BiomeWeatherAccessor) (Object) climateSettings).getDownfall();
    }

    @Inject(at = @At("TAIL"), method = "getGrassColor", cancellable = true)
    public void getSeasonGrassColor(double x, double z, CallbackInfoReturnable<Integer> cir) {
        Biome biome = (Biome) ((Object) this);
        Optional<Integer> overridedColor;
        Season.SubSeason subSeason = getCurrentSubSeason();

        if(ColorsCache.hasGrassCache(biome)) {
            overridedColor = ColorsCache.getGrassCache(biome);
        }else {
            overridedColor = specialEffects.grassColorOverride();
            Level world = Minecraft.getInstance().level;
            if(world != null) {
                Identifier biomeIdentifier = world.registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome);
                Optional<Integer> seasonGrassColor = GrassSeasonColors.getSeasonGrassColor(biome, biomeIdentifier, subSeason);
                if(seasonGrassColor.isPresent()) {
                    overridedColor = seasonGrassColor;
                }
            }
            ColorsCache.createGrassCache(biome, overridedColor);
        }
        if(specialEffects.grassColorModifier() == BiomeSpecialEffects.GrassColorModifier.SWAMP) {
            int swampColor1 = GrassSeasonColors.getSwampColor1(subSeason);
            int swampColor2 = GrassSeasonColors.getSwampColor2(subSeason);

            double d = SEASON_NOISE.getValue(x * 0.0225D, z * 0.0225D, 0.0D);
            cir.setReturnValue(d < -0.1D ? swampColor1 : swampColor2);
        }else if(overridedColor != null){
            Integer integer = overridedColor.orElseGet(this::getBaseGrassColor);
            cir.setReturnValue(specialEffects.grassColorModifier().modifyColor(x, z, integer));
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
            overridedColor = specialEffects.foliageColorOverride();
            Level world = Minecraft.getInstance().level;
            if(world != null) {
                Identifier biomeIdentifier = world.registryAccess().lookupOrThrow(Registries.BIOME).getKey(biome);
                Optional<Integer> seasonFoliageColor = FoliageSeasonColors.getSeasonFoliageColor(biome, biomeIdentifier, subSeason);
                if(seasonFoliageColor.isPresent()) {
                    overridedColor = seasonFoliageColor;
                }
            }
            ColorsCache.createFoliageCache(biome, overridedColor);
        }
        if(overridedColor != null) {
            Integer integer = overridedColor.orElseGet(this::getFoliageColorFromTexture);
            cir.setReturnValue(integer);
        }
    }

    @Inject(at = @At("HEAD"), method = "getFoliageColorFromTexture", cancellable = true)
    public void getSeasonDefaultFolliageColor(CallbackInfoReturnable<Integer> cir) {
        BiomeMixed mixed = (BiomeMixed) (Object) this;
        Season.SubSeason subSeason = getCurrentSubSeason();

        if(mixed.getOriginalTemperatureModifier() != null) {
            double originalTemperature = Mth.clamp(mixed.getOriginalTemperature(), 0.0F, 1.0F);
            double originalDownfall = Mth.clamp(mixed.getOriginalDownfall(), 0.0F, 1.0F);
            cir.setReturnValue(FoliageSeasonColors.getColor(subSeason, originalTemperature, originalDownfall));
        }else{
            double temperature = Mth.clamp(getOriginalClimateTemperature(), 0.0F, 1.0F);
            double downfall = Mth.clamp(getOriginalClimateDownfall(), 0.0F, 1.0F);
            cir.setReturnValue(FoliageSeasonColors.getColor(subSeason, temperature, downfall));
        }
    }

    @Inject(at = @At("HEAD"), method = "getBaseGrassColor", cancellable = true)
    public void getSeasonDefaultGrassColor(CallbackInfoReturnable<Integer> cir) {
        BiomeMixed mixed = (BiomeMixed) (Object) this;
        Season.SubSeason subSeason = getCurrentSubSeason();

        if(mixed.getOriginalTemperatureModifier() != null) {
            double d = Mth.clamp(mixed.getOriginalTemperature(), 0.0F, 1.0F);
            double e = Mth.clamp(mixed.getOriginalDownfall(), 0.0F, 1.0F);
            cir.setReturnValue(GrassSeasonColors.getColor(subSeason, d, e));
        }else{
            double d = Mth.clamp(getOriginalClimateTemperature(), 0.0F, 1.0F);
            double e = Mth.clamp(getOriginalClimateDownfall(), 0.0F, 1.0F);
            cir.setReturnValue(GrassSeasonColors.getColor(subSeason, d, e));
        }
    }
}
