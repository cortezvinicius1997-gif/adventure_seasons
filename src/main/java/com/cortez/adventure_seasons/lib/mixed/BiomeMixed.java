package com.cortez.adventure_seasons.lib.mixed;

import net.minecraft.world.level.biome.Biome;

public interface BiomeMixed {
    float getOriginalTemperature();
    void setOriginalTemperature(float temperature);

    boolean getOriginalHasPrecipitation();
    void setOriginalHasPrecipitation(boolean hasPrecipitation);

    float getOriginalDownfall();
    void setOriginalDownfall(float downfall);

    Biome.TemperatureModifier getOriginalTemperatureModifier();
    void setOriginalTemperatureModifier(Biome.TemperatureModifier temperatureModifier);
}
