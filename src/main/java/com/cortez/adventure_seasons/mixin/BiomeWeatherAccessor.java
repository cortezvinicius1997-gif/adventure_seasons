package com.cortez.adventure_seasons.mixin;

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin para acessar e modificar campos do Biome.ClimateSettings.
 * O tipo ClimateSettings é privado, então usamos o nome de classe completo
 * via targets. Os campos são final por padrão, então precisamos de @Mutable.
 */
@Mixin(targets = "net.minecraft.world.level.biome.Biome$ClimateSettings")
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

    @Accessor("temperatureModifier")
    Biome.TemperatureModifier getTemperatureModifier();

    @Mutable
    @Accessor("temperatureModifier")
    void setTemperatureModifier(Biome.TemperatureModifier temperatureModifier);

    @Accessor("downfall")
    float getDownfall();

    @Mutable
    @Accessor("downfall")
    void setDownfall(float downfall);
}
