package com.cortez.adventure_seasons.client;

import com.cortez.adventure_seasons.lib.AdventureSeasonClient;
import net.fabricmc.api.ClientModInitializer;

public class AdventureSeasonsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient()
    {
        AdventureSeasonClient adventureSeasonClient = new AdventureSeasonClient();
        adventureSeasonClient.init();
    }
}
