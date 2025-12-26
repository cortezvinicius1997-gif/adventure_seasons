package com.cortez.adventure_seasons.events;

import com.cortez.adventure_seasons.lib.AdventureSeason;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class PlayerTickHandler implements ServerTickEvents.StartTick {
    private AdventureSeason season;
    public PlayerTickHandler(AdventureSeason season)
    {
        this.season = season;
    }

    @Override
    public void onStartTick(MinecraftServer minecraftServer) {
        season.tick(minecraftServer);
    }
}
