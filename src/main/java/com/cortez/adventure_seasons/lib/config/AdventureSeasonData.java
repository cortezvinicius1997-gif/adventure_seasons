package com.cortez.adventure_seasons.lib.config;

import java.util.HashSet;
import java.util.Set;

public class AdventureSeasonData {

    public Set<String> excludedBiomes;
    public String season_start;
    public boolean winter_rain;
    public boolean debug;
    public SeasonLength seasonLength;
    public boolean doTemperatureChange;
    public Set<String> biomeForceSnowInWinterList;
    public boolean isFallAndSpringReversed;
    public boolean shouldSnowyBiomesMeltInSummer;
    public boolean shouldIceNearWaterMelt;
    public boolean shouldSnowReplaceVegetation;
    public boolean isServer;


    public static AdventureSeasonData defaultConfig() {
        AdventureSeasonData data = new AdventureSeasonData();

        Spring spring = new Spring();
        spring.setEarlyLength(120000);
        spring.setMidLength(480000);
        spring.setLateLength(168000);

        Summer summer = new Summer();
        summer.setEarlyLength(240000);
        summer.setMidLength(384000);
        summer.setLateLength(288000);

        Autumn autumn = new Autumn();
        autumn.setEarlyLength(192000);
        autumn.setMidLength(360000);
        autumn.setLateLength(264000);

        Winter winter = new Winter();
        winter.setEarlyLength(192000);
        winter.setMidLength(456000);
        winter.setLateLength(120000);

        SeasonLength seasonLengthdata = new SeasonLength();
        seasonLengthdata.setSpring(spring);
        seasonLengthdata.setSummer(summer);
        seasonLengthdata.setAutumn(autumn);
        seasonLengthdata.setWinter(winter);

        data.excludedBiomes = new HashSet<>();
        data.excludedBiomes.add("minecraft:desert");
        data.excludedBiomes.add("minecraft:savanna");
        data.excludedBiomes.add("minecraft:jungle");
        data.excludedBiomes.add("minecraft:sparse_jungle");
        data.excludedBiomes.add("minecraft:badlands");
        data.excludedBiomes.add("minecraft:eroded_badlands");
        data.excludedBiomes.add("terralith:glacial_chasm");
        data.excludedBiomes.add("minecraft:frozen_ocean");
        data.excludedBiomes.add("minecraft:deep_frozen_ocean");
        data.excludedBiomes.add("minecraft:cold_ocean");
        data.excludedBiomes.add("minecraft:deep_cold_ocean");
        data.excludedBiomes.add("minecraft:ocean");
        data.excludedBiomes.add("minecraft:lukewarm_ocean");
        data.excludedBiomes.add("minecraft:deep_lukewarm_ocean");
        data.excludedBiomes.add("minecraft:warm_ocean");
        data.season_start = "SPRING";
        data.winter_rain = true;
        data.debug = false;
        data.doTemperatureChange = true;
        data.seasonLength = seasonLengthdata;
        data.biomeForceSnowInWinterList = new HashSet<>();
        data.biomeForceSnowInWinterList.add("minecraft:plains");
        data.biomeForceSnowInWinterList.add("minecraft:sunflower_plains");
        data.biomeForceSnowInWinterList.add("minecraft:stony_peaks");
        data.isFallAndSpringReversed = true;
        data.shouldSnowyBiomesMeltInSummer = true;
        data.shouldIceNearWaterMelt = false;
        data.shouldSnowReplaceVegetation = true;
        data.isServer = true;



        return data;
    }
}
