package com.cortez.adventure_seasons.mixin;

import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin para acessar campos privados da classe Biome.
 * Necessário para modificar temperatura e precipitação em tempo de execução.
 */
@Mixin(Biome.class)
public interface BiomeAccessor {

    @Accessor("weather")
    Biome.Weather getWeather();

    @Accessor("weather")
    void setWeather(Biome.Weather weather);
}

