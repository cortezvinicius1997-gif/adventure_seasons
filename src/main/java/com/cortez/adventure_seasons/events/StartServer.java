package com.cortez.adventure_seasons.events;

import com.cortez.adventure_seasons.lib.AdventureSeason;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class StartServer {
    public static AdventureSeason register(AdventureSeason adventureSeason)
    {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {

            adventureSeason.init(server, false);
        });
        return adventureSeason;
    }
}
