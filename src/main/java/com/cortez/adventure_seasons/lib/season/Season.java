package com.cortez.adventure_seasons.lib.season;

import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public enum Season implements StringIdentifiable
{
    SPRING("season.adventure_seasons.spring", "spring"),
    SUMMER("season.adventure_seasons.summer", "summer"),
    AUTUMN("season.adventure_seasons.autumn", "autumn"),
    WINTER("season.adventure_seasons.winter", "winter");



    public enum SubSeason implements StringIdentifiable{
        EARLY_SPRING(SPRING, "subseason.adventure_seasons.early", "early_spring"),
        MID_SPRING(SPRING, "subseason.adventure_seasons.mid", "mid_spring"),
        LATE_SPRING(SPRING, "subseason.adventure_seasons.late", "late_spring"),

        EARLY_SUMMER(SUMMER, "subseason.adventure_seasons.early", "early_summer"),
        MID_SUMMER(SUMMER, "subseason.adventure_seasons.mid", "mid_summer"),
        LATE_SUMMER(SUMMER, "subseason.adventure_seasons.late", "late_summer"),

        EARLY_AUTUMN(AUTUMN, "subseason.adventure_seasons.early", "early_autumn"),
        MID_AUTUMN(AUTUMN, "subseason.adventure_seasons.mid", "mid_autumn"),
        LATE_AUTUMN(AUTUMN, "subseason.adventure_seasons.late", "late_autumn"),

        EARLY_WINTER(WINTER, "subseason.adventure_seasons.early", "early_winter"),
        MID_WINTER(WINTER, "subseason.adventure_seasons.mid", "mid_winter"),
        LATE_WINTER(WINTER, "subseason.adventure_seasons.late", "late_winter");

        private final String translationKey;
        private final Season season;
        private final String id;

        SubSeason(Season season, String translationKey, String id) {
            this.season = season;
            this.translationKey = translationKey;
            this.id = id;
        }

        public Season getSeason() {
            return season;
        }

        public Text getDisplayName() {
            return Text.translatable(translationKey);
        }

        @Override
        public String asString() {
            return id;
        }
    }

    private final String translationKey;
    private final String id;

    Season(String translationKey, String id) {
        this.translationKey = translationKey;
        this.id = id;
    }


    public String getId(){
        return id;
    }

    @Override
    public String asString() {
        return id;
    }

    public Text getDisplayName() {
        return Text.translatable(translationKey);
    }
}
