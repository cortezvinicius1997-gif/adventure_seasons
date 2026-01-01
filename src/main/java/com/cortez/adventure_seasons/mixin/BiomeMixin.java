package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.mixed.BiomeMixed;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Biome.class)
public abstract class BiomeMixin implements BiomeMixed
{
    @Shadow @Final public Biome.Weather weather;

    private Biome.Weather originalWeather;

    @Override
    public Biome.Weather getOriginalWeather() {
        return originalWeather;
    }

    @Override
    public void setOriginalWeather(Biome.Weather originalWeather) {
        this.originalWeather = originalWeather;
    }
}