package com.cortez.adventure_seasons.lib.util;

import net.minecraft.world.level.biome.Biome;

import java.lang.reflect.Field;

/**
 * Utilidade para acessar o campo privado climateSettings da classe Biome.
 * O tipo real (Biome$ClimateSettings) é privado e não pode ser referenciado
 * diretamente em Java, então usamos reflexão (com cache do Field) para ler
 * e escrever o objeto ClimateSettings. Os campos individuais desse record
 * são manipulados via BiomeWeatherAccessor.
 */
public final class BiomeAccessor {

    private static Field CLIMATE_SETTINGS_FIELD;

    private BiomeAccessor() {
    }

    private static Field getClimateSettingsField() {
        Field field = CLIMATE_SETTINGS_FIELD;
        if (field == null) {
            try {
                field = Biome.class.getDeclaredField("climateSettings");
                field.setAccessible(true);
                CLIMATE_SETTINGS_FIELD = field;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not find Biome.climateSettings field", e);
            }
        }
        return field;
    }

    public static Object getClimateSettings(Biome biome) {
        try {
            return getClimateSettingsField().get(biome);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not read Biome.climateSettings", e);
        }
    }

    public static void setClimateSettings(Biome biome, Object climateSettings) {
        try {
            getClimateSettingsField().set(biome, climateSettings);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not write Biome.climateSettings", e);
        }
    }
}
