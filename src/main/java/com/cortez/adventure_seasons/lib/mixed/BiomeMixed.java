package com.cortez.adventure_seasons.lib.mixed;

import net.minecraft.world.biome.Biome;

public interface BiomeMixed {
    Biome.Weather getOriginalWeather();
    void setOriginalWeather(Biome.Weather originalWeather);
}
