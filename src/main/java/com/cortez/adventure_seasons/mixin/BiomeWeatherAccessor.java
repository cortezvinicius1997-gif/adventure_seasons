package com.cortez.adventure_seasons.mixin;

import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin para acessar e modificar campos do Biome.Weather.
 * Os campos são final por padrão, então precisamos de @Mutable para modificá-los.
 */
@Mixin(Biome.Weather.class)
public interface BiomeWeatherAccessor {

    @Accessor("hasPrecipitation")
    boolean getHasPrecipitation();

    @Mutable
    @Accessor("hasPrecipitation")
    void setHasPrecipitation(boolean hasPrecipitation);

    @Accessor("temperature")
    float getTemperature();

    @Mutable
    @Accessor("temperature")
    void setTemperature(float temperature);

    @Accessor("downfall")
    float getDownfall();

    @Mutable
    @Accessor("downfall")
    void setDownfall(float downfall);
}

