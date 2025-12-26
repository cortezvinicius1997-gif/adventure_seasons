package com.cortez.adventure_seasons.lib.util;

import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.Season.SubSeason;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SeasonColor {
    private final int springColor;
    private final int summerColor;
    private final int autumnColor;
    private final int winterColor;

    // Cores para subestações (opcional)
    private final Integer earlySpringColor;
    private final Integer midSpringColor;
    private final Integer lateSpringColor;

    private final Integer earlySummerColor;
    private final Integer midSummerColor;
    private final Integer lateSummerColor;

    private final Integer earlyAutumnColor;
    private final Integer midAutumnColor;
    private final Integer lateAutumnColor;

    private final Integer earlyWinterColor;
    private final Integer midWinterColor;
    private final Integer lateWinterColor;

    public SeasonColor(int springColor, int summerColor, int autumnColor, int winterColor) {
        this.springColor = springColor;
        this.summerColor = summerColor;
        this.autumnColor = autumnColor;
        this.winterColor = winterColor;

        // Sem cores específicas de subestação
        this.earlySpringColor = null;
        this.midSpringColor = null;
        this.lateSpringColor = null;
        this.earlySummerColor = null;
        this.midSummerColor = null;
        this.lateSummerColor = null;
        this.earlyAutumnColor = null;
        this.midAutumnColor = null;
        this.lateAutumnColor = null;
        this.earlyWinterColor = null;
        this.midWinterColor = null;
        this.lateWinterColor = null;
    }

    public SeasonColor(JsonElement json) {
        JsonObject obj = json.getAsJsonObject();

        // Cores principais (obrigatórias)
        this.springColor = getStringColor(obj.get("spring").getAsString());
        this.summerColor = getStringColor(obj.get("summer").getAsString());
        this.autumnColor = getStringColor(obj.get("autumn").getAsString());
        this.winterColor = getStringColor(obj.get("winter").getAsString());

        // Cores de subestações (opcionais)
        this.earlySpringColor = getOptionalColor(obj, "early_spring");
        this.midSpringColor = getOptionalColor(obj, "mid_spring");
        this.lateSpringColor = getOptionalColor(obj, "late_spring");

        this.earlySummerColor = getOptionalColor(obj, "early_summer");
        this.midSummerColor = getOptionalColor(obj, "mid_summer");
        this.lateSummerColor = getOptionalColor(obj, "late_summer");

        this.earlyAutumnColor = getOptionalColor(obj, "early_autumn");
        this.midAutumnColor = getOptionalColor(obj, "mid_autumn");
        this.lateAutumnColor = getOptionalColor(obj, "late_autumn");

        this.earlyWinterColor = getOptionalColor(obj, "early_winter");
        this.midWinterColor = getOptionalColor(obj, "mid_winter");
        this.lateWinterColor = getOptionalColor(obj, "late_winter");
    }

    private Integer getOptionalColor(JsonObject obj, String key) {
        if (obj.has(key)) {
            return getStringColor(obj.get(key).getAsString());
        }
        return null;
    }

    private int getStringColor(String color) {
        if(color.startsWith("0x")) {
            return Integer.parseInt(color.replace("0x", ""), 16);
        }else if(color.startsWith("#")) {
            return Integer.parseInt(color.replace("#", ""), 16);
        }else{
            return Integer.parseInt(color);
        }
    }

    public int getColor(Season season) {
        return switch (season) {
            case SPRING -> springColor;
            case SUMMER -> summerColor;
            case AUTUMN -> autumnColor;
            case WINTER -> winterColor;
        };
    }

    /**
     * Obtém a cor para uma subestação específica.
     * Se não houver cor definida para a subestação, retorna a cor da estação principal.
     */
    public int getColor(SubSeason subSeason) {
        Integer subSeasonColor = switch (subSeason) {
            case EARLY_SPRING -> earlySpringColor;
            case MID_SPRING -> midSpringColor;
            case LATE_SPRING -> lateSpringColor;
            case EARLY_SUMMER -> earlySummerColor;
            case MID_SUMMER -> midSummerColor;
            case LATE_SUMMER -> lateSummerColor;
            case EARLY_AUTUMN -> earlyAutumnColor;
            case MID_AUTUMN -> midAutumnColor;
            case LATE_AUTUMN -> lateAutumnColor;
            case EARLY_WINTER -> earlyWinterColor;
            case MID_WINTER -> midWinterColor;
            case LATE_WINTER -> lateWinterColor;
        };

        // Se a cor da subestação não estiver definida, usa a cor da estação principal
        return subSeasonColor != null ? subSeasonColor : getColor(subSeason.getSeason());
    }
}