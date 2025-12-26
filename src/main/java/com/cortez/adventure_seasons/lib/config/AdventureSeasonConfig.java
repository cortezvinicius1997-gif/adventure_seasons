package com.cortez.adventure_seasons.lib.config;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.lib.season.Season;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class AdventureSeasonConfig
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = AdventureSeasons.MODID+".json";
    private static AdventureSeasonData data;

    public static void load(){
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

        }catch (Exception e){
            AdventureSeasons.LOGGER.error("[Adventure Mod] Erro ao carregar configuração: " + e.getMessage());
            data = AdventureSeasonData.defaultConfig();
            save(configFile);
        }
    }

    private static void validateConfig() {

    }

    private static void save(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(data, writer);
        } catch (Exception e) {
            AdventureSeasons.LOGGER.error("[Adventure Mod] Erro ao salvar configuração: " + e.getMessage());
        }
    }

    public static boolean isWinterRain() {
        return data.winter_rain;
    }

    public static Season getStartingSeason()
    {
        if (data.season_start == null) {
            return Season.SPRING;
        }
        try {
            return Season.valueOf(data.season_start.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Season.SPRING;
        }
    }

    public static Season.SubSeason getStartingSubSeason() {
        Season startSeason = getStartingSeason(); // Seu método existente que retorna Season

        return switch (startSeason) {
            case SPRING -> Season.SubSeason.EARLY_SPRING;
            case SUMMER -> Season.SubSeason.EARLY_SUMMER;
            case AUTUMN -> Season.SubSeason.EARLY_AUTUMN;
            case WINTER -> Season.SubSeason.EARLY_WINTER;
        };
    }

    public static boolean isExcludedBiome(Identifier biomeId)
    {

        if (biomeId == null) {
            return false;
        }


        if (data.excludedBiomes == null) {
            return false;
        }
        return data.excludedBiomes.contains(biomeId.toString());
    }

    public static boolean isDebug() {
        return data.debug;
    }

    public static SeasonLength getTicksPerSeason() {
        return data.seasonLength;
    }

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

    public static boolean doTemperatureChanges(Identifier biomeId)
    {
        return data.doTemperatureChange && !data.excludedBiomes.contains(biomeId.toString());
    }


    public static boolean isSnowForcedInBiome(Identifier biomeId) {
        return data.biomeForceSnowInWinterList.contains(biomeId.toString());
    }

    public static boolean isFallAndSpringReversed() {
        return data.isFallAndSpringReversed;
    }



    public static boolean shouldSnowyBiomesMeltInSummer() {
        return data.shouldSnowyBiomesMeltInSummer;
    }



    public static boolean isShouldIceNearWaterMelt() {
        return data.shouldIceNearWaterMelt;
    }

    public static boolean shouldSnowReplaceVegetation()
    {
        return data.shouldSnowReplaceVegetation;
    }
}
