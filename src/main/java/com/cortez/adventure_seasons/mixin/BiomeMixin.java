package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.mixed.BiomeMixed;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Biome.class)
public abstract class BiomeMixin implements BiomeMixed
{
    private float originalTemperature;
    private boolean originalHasPrecipitation;
    private float originalDownfall;
    private Biome.TemperatureModifier originalTemperatureModifier;

    @Override
    public float getOriginalTemperature() {
        return originalTemperature;
    }

    @Override
    public void setOriginalTemperature(float temperature) {
        this.originalTemperature = temperature;
    }

    @Override
    public boolean getOriginalHasPrecipitation() {
        return originalHasPrecipitation;
    }

    @Override
    public void setOriginalHasPrecipitation(boolean hasPrecipitation) {
        this.originalHasPrecipitation = hasPrecipitation;
    }

    @Override
    public float getOriginalDownfall() {
        return originalDownfall;
    }

    @Override
    public void setOriginalDownfall(float downfall) {
        this.originalDownfall = downfall;
    }

    @Override
    public Biome.TemperatureModifier getOriginalTemperatureModifier() {
        return originalTemperatureModifier;
    }

    @Override
    public void setOriginalTemperatureModifier(Biome.TemperatureModifier temperatureModifier) {
        this.originalTemperatureModifier = temperatureModifier;
    }
}
