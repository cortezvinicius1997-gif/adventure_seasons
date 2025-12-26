package com.cortez.adventure_seasons.lib.resources;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.lib.AdventureSeason;
import com.cortez.adventure_seasons.lib.season.Season.SubSeason;
import com.cortez.adventure_seasons.lib.util.SeasonColor;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.util.RawTextureDataLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

public class GrassSeasonColors implements SimpleSynchronousResourceReloadListener
{
    // Colormaps para cada subestação
    private static final Identifier EARLY_SPRING_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/early_spring_grass.png");
    private static final Identifier MID_SPRING_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/mid_spring_grass.png");
    private static final Identifier LATE_SPRING_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/late_spring_grass.png");

    private static final Identifier EARLY_SUMMER_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/early_summer_grass.png");
    private static final Identifier MID_SUMMER_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/mid_summer_grass.png");
    private static final Identifier LATE_SUMMER_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/late_summer_grass.png");

    private static final Identifier EARLY_AUTUMN_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/early_autumn_grass.png");
    private static final Identifier MID_AUTUMN_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/mid_autumn_grass.png");
    private static final Identifier LATE_AUTUMN_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/late_autumn_grass.png");

    private static final Identifier EARLY_WINTER_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/early_winter_grass.png");
    private static final Identifier MID_WINTER_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/mid_winter_grass.png");
    private static final Identifier LATE_WINTER_GRASS_COLORMAP = AdventureSeason.identifier("textures/colormap/late_winter_grass.png");

    // Arrays de cores para cada subestação
    private static int[] earlySpringColorMap = new int[65536];
    private static int[] midSpringColorMap = new int[65536];
    private static int[] lateSpringColorMap = new int[65536];

    private static int[] earlySummerColorMap = new int[65536];
    private static int[] midSummerColorMap = new int[65536];
    private static int[] lateSummerColorMap = new int[65536];

    private static int[] earlyAutumnColorMap = new int[65536];
    private static int[] midAutumnColorMap = new int[65536];
    private static int[] lateAutumnColorMap = new int[65536];

    private static int[] earlyWinterColorMap = new int[65536];
    private static int[] midWinterColorMap = new int[65536];
    private static int[] lateWinterColorMap = new int[65536];

    private static SeasonColor minecraftSwampGrass1;
    private static SeasonColor minecraftSwampGrass2;

    private static final HashMap<Identifier, SeasonColor> grassColorMap = new HashMap<>();

    public static Optional<Integer> getSeasonGrassColor(Biome biome, Identifier biomeIdentifier, SubSeason subSeason) {
        Optional<SeasonColor> colors;
        if(grassColorMap.containsKey(biomeIdentifier)) {
            colors = Optional.of(grassColorMap.get(biomeIdentifier));
        }else{
            colors = Optional.empty();
        }
        return colors.map(seasonColor -> seasonColor.getColor(subSeason));
    }

    public static int getColor(SubSeason subSeason, double temperature, double humidity) {
        humidity *= temperature;
        int i = (int)((1.0D - temperature) * 255.0D);
        int j = (int)((1.0D - humidity) * 255.0D);
        int k = j << 8 | i;

        int[] colorMap = switch (subSeason) {
            case EARLY_SPRING -> earlySpringColorMap;
            case MID_SPRING -> midSpringColorMap;
            case LATE_SPRING -> lateSpringColorMap;
            case EARLY_SUMMER -> earlySummerColorMap;
            case MID_SUMMER -> midSummerColorMap;
            case LATE_SUMMER -> lateSummerColorMap;
            case EARLY_AUTUMN -> earlyAutumnColorMap;
            case MID_AUTUMN -> midAutumnColorMap;
            case LATE_AUTUMN -> lateAutumnColorMap;
            case EARLY_WINTER -> earlyWinterColorMap;
            case MID_WINTER -> midWinterColorMap;
            case LATE_WINTER -> lateWinterColorMap;
        };

        return k > colorMap.length ? -65281 : colorMap[k];
    }

    public static int getSwampColor1(SubSeason subSeason) {
        return minecraftSwampGrass1.getColor(subSeason);
    }

    public static int getSwampColor2(SubSeason subSeason) {
        return minecraftSwampGrass2.getColor(subSeason);
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(AdventureSeasons.MODID, "grass_season_colors");
    }

    @Override
    public void reload(ResourceManager manager) {
        try{
            Resource swampGrass1 = manager.getResource(AdventureSeason.identifier("hardcoded/grass/swamp1.json")).orElseThrow();
            minecraftSwampGrass1 = new SeasonColor(JsonParser.parseReader(new InputStreamReader(swampGrass1.getInputStream(), StandardCharsets.UTF_8)));
            Resource swampGrass2 = manager.getResource(AdventureSeason.identifier("hardcoded/grass/swamp2.json")).orElseThrow();
            minecraftSwampGrass2 = new SeasonColor(JsonParser.parseReader(new InputStreamReader(swampGrass2.getInputStream(), StandardCharsets.UTF_8)));
        }catch (Exception e) {
            AdventureSeasons.LOGGER.error("[Adventure Mod] Failed to load hardcoded grass colors", e);
        }

        grassColorMap.clear();
        manager.findResources("seasons/grass", id -> id.getPath().endsWith(".json")).forEach((id, resource) -> {
            String[] split = id.getPath().split("/");
            Identifier biomeIdentifier = Identifier.of(id.getNamespace(), split[split.length-1].replace(".json", ""));
            try {
                SeasonColor colors = new SeasonColor(JsonParser.parseReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)));
                grassColorMap.put(biomeIdentifier, colors);
            }catch(Exception e) {
                AdventureSeasons.LOGGER.error("[Adventure Mod] Failed to load biome grass colors for: "+biomeIdentifier, e);
            }
        });
        if(!grassColorMap.isEmpty()) {
            AdventureSeasons.LOGGER.info("[Adventure Mod] Successfully loaded "+grassColorMap.size()+" custom grass colors.");
        }

        // Carregar todos os 12 colormaps
        try {
            earlySpringColorMap = RawTextureDataLoader.loadRawTextureData(manager, EARLY_SPRING_GRASS_COLORMAP);
            midSpringColorMap = RawTextureDataLoader.loadRawTextureData(manager, MID_SPRING_GRASS_COLORMAP);
            lateSpringColorMap = RawTextureDataLoader.loadRawTextureData(manager, LATE_SPRING_GRASS_COLORMAP);

            earlySummerColorMap = RawTextureDataLoader.loadRawTextureData(manager, EARLY_SUMMER_GRASS_COLORMAP);
            midSummerColorMap = RawTextureDataLoader.loadRawTextureData(manager, MID_SUMMER_GRASS_COLORMAP);
            lateSummerColorMap = RawTextureDataLoader.loadRawTextureData(manager, LATE_SUMMER_GRASS_COLORMAP);

            earlyAutumnColorMap = RawTextureDataLoader.loadRawTextureData(manager, EARLY_AUTUMN_GRASS_COLORMAP);
            midAutumnColorMap = RawTextureDataLoader.loadRawTextureData(manager, MID_AUTUMN_GRASS_COLORMAP);
            lateAutumnColorMap = RawTextureDataLoader.loadRawTextureData(manager, LATE_AUTUMN_GRASS_COLORMAP);

            earlyWinterColorMap = RawTextureDataLoader.loadRawTextureData(manager, EARLY_WINTER_GRASS_COLORMAP);
            midWinterColorMap = RawTextureDataLoader.loadRawTextureData(manager, MID_WINTER_GRASS_COLORMAP);
            lateWinterColorMap = RawTextureDataLoader.loadRawTextureData(manager, LATE_WINTER_GRASS_COLORMAP);

            AdventureSeasons.LOGGER.info("[Adventure Mod] Successfully loaded all 12 subseason grass colormaps.");
        } catch (IOException e) {
            AdventureSeasons.LOGGER.error("[Adventure Mod] Failed to load grass color textures", e);
        }
    }
}