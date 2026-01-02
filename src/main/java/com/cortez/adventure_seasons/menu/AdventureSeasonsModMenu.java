package com.cortez.adventure_seasons.menu;

import com.cortez.adventure_seasons.lib.config.screen.AdventureSeasonConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class AdventureSeasonsModMenu implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return AdventureSeasonConfigScreen::create;
    }
}
