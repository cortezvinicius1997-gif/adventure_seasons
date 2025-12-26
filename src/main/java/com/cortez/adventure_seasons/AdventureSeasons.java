package com.cortez.adventure_seasons;

import com.cortez.adventure_seasons.util.ModRegisterSeason;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class AdventureSeasons implements ModInitializer {

    public static final String MODID = "adventure_seasons";
    public static final Logger LOGGER = LogManager.getLogger(MODID);


    @Override
    public void onInitialize()
    {
        ModRegisterSeason.register();
    }
}
