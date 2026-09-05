package com.cortez.adventure_seasons.lib.config;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.lib.season.Season;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class AdventureSeasonConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = AdventureSeasons.MODID + ".json";
    private static AdventureSeasonData data;

    public static void load() {
        File configFile = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(FILE_NAME)
                .toFile();

        if (!configFile.exists()) {
            data = AdventureSeasonData.defaultConfig();
            save(configFile);
            AdventureSeasons.LOGGER.info("[Adventure Mod] Arquivo de configuração criado: " + configFile.getAbsolutePath());
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            data = GSON.fromJson(reader, AdventureSeasonData.class);
            if (data == null) {
                data = AdventureSeasonData.defaultConfig();
                save(configFile);
                return;
            }
            validateConfig();
            AdventureSeasons.LOGGER.info("[Adventure Mod] Configuração carregada:");
        } catch (Exception e) {
            AdventureSeasons.LOGGER.error("[Adventure Mod] Erro ao carregar configuração: " + e.getMessage());
            data = AdventureSeasonData.defaultConfig();
            save(configFile);
        }
    }

    private static void validateConfig() {
        boolean hasChanges = false;

        if (data.season_start == null || data.season_start.isEmpty()) {
            data.season_start = "SPRING";
            hasChanges = true;
        } else {
            try {
                Season.valueOf(data.season_start.toUpperCase());
            } catch (IllegalArgumentException e) {
                data.season_start = "SPRING";
                hasChanges = true;
            }
        }

        if (data.excludedBiomes == null) {
            data.excludedBiomes = new java.util.HashSet<>();
            hasChanges = true;
        }
        if (data.biomeForceSnowInWinterList == null) {
            data.biomeForceSnowInWinterList = new java.util.HashSet<>();
            hasChanges = true;
        }
        if (data.seasonLength == null) {
            AdventureSeasonData defaultData = AdventureSeasonData.defaultConfig();
            data.seasonLength = defaultData.seasonLength;
            hasChanges = true;
        }

        if (data.springSaplingSpawnEnabled == null) {
            data.springSaplingSpawnEnabled = true;
            hasChanges = true;
        }
        if (data.springSaplingSpawnChance == null) {
            data.springSaplingSpawnChance = 0.003;
            hasChanges = true;
        }
        if (data.maxSaplingsPerChunk == null) {
            data.maxSaplingsPerChunk = 1;
            hasChanges = true;
        }
        if (data.springVegetationSpawnChance == null) {
            data.springVegetationSpawnChance = 0.02;
            hasChanges = true;
        }
        if (data.springVegetationSpawnEnabled == null) {
            data.springVegetationSpawnEnabled = true;
            hasChanges = true;
        }
        if (data.maxVegetationPerChunk == null) {
            data.maxVegetationPerChunk = 40;
            hasChanges = true;
        }

        if (hasChanges) {
            File configFile = FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve(FILE_NAME)
                    .toFile();
            save(configFile);
        }
    }

    private static void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(data, writer);
        } catch (Exception e) {
            AdventureSeasons.LOGGER.error("[Adventure Mod] Erro ao salvar configuração: " + e.getMessage());
        }
    }

    public static boolean isWinterRain() { return data.winter_rain; }

    private static File configFile() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME).toFile();
    }

    public static Season getStartingSeason() {
        if (data.season_start == null) return Season.SPRING;
        try {
            return Season.valueOf(data.season_start.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Season.SPRING;
        }
    }

    public static Season.SubSeason getStartingSubSeason() {
        Season startSeason = getStartingSeason();
        return switch (startSeason) {
            case SPRING -> Season.SubSeason.EARLY_SPRING;
            case SUMMER -> Season.SubSeason.EARLY_SUMMER;
            case AUTUMN -> Season.SubSeason.EARLY_AUTUMN;
            case WINTER -> Season.SubSeason.EARLY_WINTER;
        };
    }

    public static boolean isExcludedBiome(Identifier biomeId) {
        if (biomeId == null) return false;
        if (data.excludedBiomes == null) return false;
        return data.excludedBiomes.contains(biomeId.toString());
    }

    public static boolean isDebug() { return data.debug; }
    public static SeasonLength getTicksPerSeason() { return data.seasonLength; }

    public static int getTicksForSubSeason(Season.SubSeason subSeason) {
        SeasonLength seasonLength = getTicksPerSeason();
        return switch (subSeason) {
            case EARLY_SPRING -> seasonLength.getSpring().getEarlyLength();
            case MID_SPRING -> seasonLength.getSpring().getMidLength();
            case LATE_SPRING -> seasonLength.getSpring().getLateLength();
            case EARLY_SUMMER -> seasonLength.getSummer().getEarlyLength();
            case MID_SUMMER -> seasonLength.getSummer().getMidLength();
            case LATE_SUMMER -> seasonLength.getSummer().getLateLength();
            case EARLY_AUTUMN -> seasonLength.getAutumn().getEarlyLength();
            case MID_AUTUMN -> seasonLength.getAutumn().getMidLength();
            case LATE_AUTUMN -> seasonLength.getAutumn().getLateLength();
            case EARLY_WINTER -> seasonLength.getWinter().getEarlyLength();
            case MID_WINTER -> seasonLength.getWinter().getMidLength();
            case LATE_WINTER -> seasonLength.getWinter().getLateLength();
        };
    }

    public static boolean doTemperatureChanges(Identifier biomeId) {
        if (biomeId == null || data.excludedBiomes == null) return data.doTemperatureChange;
        return data.doTemperatureChange && !data.excludedBiomes.contains(biomeId.toString());
    }

    public static boolean isSnowForcedInBiome(Identifier biomeId) {
        if (biomeId == null || data.biomeForceSnowInWinterList == null) return false;
        return data.biomeForceSnowInWinterList.contains(biomeId.toString());
    }

    public static boolean isDoTemperatureChange() { return data.doTemperatureChange; }

    public static java.util.Set<String> getExcludedBiomes() {
        return data.excludedBiomes == null ? new java.util.HashSet<>() : data.excludedBiomes;
    }

    public static java.util.Set<String> getBiomeForceSnowInWinterList() {
        return data.biomeForceSnowInWinterList == null ? new java.util.HashSet<>() : data.biomeForceSnowInWinterList;
    }

    public static boolean isFallAndSpringReversed() { return data.isFallAndSpringReversed; }
    public static boolean shouldSnowyBiomesMeltInSummer() { return data.shouldSnowyBiomesMeltInSummer; }
    public static boolean isShouldIceNearWaterMelt() { return data.shouldIceNearWaterMelt; }
    public static boolean shouldSnowReplaceVegetation() { return data.shouldSnowReplaceVegetation; }
    public static boolean isServer() { return data.isServer; }

    /** Se falso, saplings nunca nascem sozinhas na primavera (grama/flores continuam nascendo normalmente). */
    public static boolean isSpringSaplingSpawnEnabled() {
        return data.springSaplingSpawnEnabled == null || data.springSaplingSpawnEnabled;
    }

    /** Chance (0.0 a 1.0) de uma posição elegível virar uma sapling, ao invés de grama/flor. Padrão: 0.003 (0.3%). */
    public static double getSpringSaplingSpawnChance() {
        return data.springSaplingSpawnChance == null ? 0.003 : data.springSaplingSpawnChance;
    }

    /** Máximo de saplings que podem nascer sozinhas por chunk durante uma mesma primavera. Padrão: 1. */
    public static int getMaxSaplingsPerChunk() {
        return data.maxSaplingsPerChunk == null ? 1 : data.maxSaplingsPerChunk;
    }

    /** Chance (0.0 a 1.0) de uma posição elegível virar grama/flor na primavera. Padrão: 0.02 (2%). */
    public static double getSpringVegetationSpawnChance() {
        return data.springVegetationSpawnChance == null ? 0.02 : data.springVegetationSpawnChance;
    }

    /** Se falso, grama/flores nunca nascem sozinhas na primavera (mudas continuam seguindo sua própria config). */
    public static boolean isSpringVegetationSpawnEnabled() {
        return data.springVegetationSpawnEnabled == null || data.springVegetationSpawnEnabled;
    }

    /** Máximo de plantas (grama/flores) que podem nascer sozinhas por chunk durante uma mesma primavera. Padrão: 40. */
    public static int getMaxVegetationPerChunk() {
        return data.maxVegetationPerChunk == null ? 40 : data.maxVegetationPerChunk;
    }

    public static void setServer(boolean isServer) {
        data.isServer = isServer;
        File configFile = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(FILE_NAME)
                .toFile();
        save(configFile);
    }

    public static void setSpringSaplingSpawnEnabled(boolean enabled) {
        data.springSaplingSpawnEnabled = enabled;
        save(FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME).toFile());
    }

    public static void setSpringSaplingSpawnChance(double chance) {
        data.springSaplingSpawnChance = chance;
        save(FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME).toFile());
    }

    public static void setMaxSaplingsPerChunk(int max) {
        data.maxSaplingsPerChunk = max;
        save(FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME).toFile());
    }

    public static void setSpringVegetationSpawnChance(double chance) {
        data.springVegetationSpawnChance = chance;
        save(configFile());
    }

    public static void setSpringVegetationSpawnEnabled(boolean enabled) {
        data.springVegetationSpawnEnabled = enabled;
        save(configFile());
    }

    public static void setMaxVegetationPerChunk(int max) {
        data.maxVegetationPerChunk = max;
        save(configFile());
    }

    // ==== Setters gerados para exibir/editar todas as opções do JSON no ClothConfig ====

    public static void setDebug(boolean value) {
        data.debug = value;
        save(configFile());
    }

    public static void setWinterRain(boolean value) {
        data.winter_rain = value;
        save(configFile());
    }

    public static void setDoTemperatureChange(boolean value) {
        data.doTemperatureChange = value;
        save(configFile());
    }

    public static void setFallAndSpringReversed(boolean value) {
        data.isFallAndSpringReversed = value;
        save(configFile());
    }

    public static void setShouldSnowyBiomesMeltInSummer(boolean value) {
        data.shouldSnowyBiomesMeltInSummer = value;
        save(configFile());
    }

    public static void setShouldIceNearWaterMelt(boolean value) {
        data.shouldIceNearWaterMelt = value;
        save(configFile());
    }

    public static void setShouldSnowReplaceVegetation(boolean value) {
        data.shouldSnowReplaceVegetation = value;
        save(configFile());
    }

    /** Aceita "SPRING", "SUMMER", "AUTUMN" ou "WINTER" (case-insensitive). Ignora valores inválidos. */
    public static void setSeasonStart(Season season) {
        data.season_start = season.name();
        save(configFile());
    }

    public static void setExcludedBiomes(java.util.List<String> biomes) {
        data.excludedBiomes = new java.util.HashSet<>(biomes);
        save(configFile());
    }

    public static void setBiomeForceSnowInWinterList(java.util.List<String> biomes) {
        data.biomeForceSnowInWinterList = new java.util.HashSet<>(biomes);
        save(configFile());
    }

    // Duração das subestações (em ticks). 24000 ticks = 1 dia in-game.
    public static void setSpringEarlyLength(int ticks) { data.seasonLength.getSpring().setEarlyLength(ticks); save(configFile()); }
    public static void setSpringMidLength(int ticks) { data.seasonLength.getSpring().setMidLength(ticks); save(configFile()); }
    public static void setSpringLateLength(int ticks) { data.seasonLength.getSpring().setLateLength(ticks); save(configFile()); }

    public static void setSummerEarlyLength(int ticks) { data.seasonLength.getSummer().setEarlyLength(ticks); save(configFile()); }
    public static void setSummerMidLength(int ticks) { data.seasonLength.getSummer().setMidLength(ticks); save(configFile()); }
    public static void setSummerLateLength(int ticks) { data.seasonLength.getSummer().setLateLength(ticks); save(configFile()); }

    public static void setAutumnEarlyLength(int ticks) { data.seasonLength.getAutumn().setEarlyLength(ticks); save(configFile()); }
    public static void setAutumnMidLength(int ticks) { data.seasonLength.getAutumn().setMidLength(ticks); save(configFile()); }
    public static void setAutumnLateLength(int ticks) { data.seasonLength.getAutumn().setLateLength(ticks); save(configFile()); }

    public static void setWinterEarlyLength(int ticks) { data.seasonLength.getWinter().setEarlyLength(ticks); save(configFile()); }
    public static void setWinterMidLength(int ticks) { data.seasonLength.getWinter().setMidLength(ticks); save(configFile()); }
    public static void setWinterLateLength(int ticks) { data.seasonLength.getWinter().setLateLength(ticks); save(configFile()); }
}