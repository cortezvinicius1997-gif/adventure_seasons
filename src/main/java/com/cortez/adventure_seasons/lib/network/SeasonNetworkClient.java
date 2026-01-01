package com.cortez.adventure_seasons.lib.network;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.lib.cache.ColorsCache;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;

public class SeasonNetworkClient {
    // Estado da estação sincronizado do servidor
    private static Season.SubSeason clientSubSeason = Season.SubSeason.EARLY_SPRING;
    private static int clientTicks = 0;
    private static boolean initialized = false;

    public static void init() {
        // Registra o tipo de payload no cliente
        PayloadTypeRegistry.playS2C().register(SeasonSyncPayload.ID, SeasonSyncPayload.CODEC);

        // Registra o handler do pacote
        ClientPlayNetworking.registerGlobalReceiver(SeasonSyncPayload.ID, (payload, context) -> {
            Season.SubSeason newSubSeason = payload.getSubSeason();
            int newTicks = payload.getTicks();

            context.client().execute(() -> {
                Season.SubSeason oldSubSeason = clientSubSeason;
                clientSubSeason = newSubSeason;
                clientTicks = newTicks;
                initialized = true;

                // Atualiza também o SeasonState estático para uso em mixins
                SeasonState.updateFromServer(newSubSeason, newTicks);

                AdventureSeasons.LOGGER.info("[Adventure Seasons Client] Estação sincronizada do servidor: " + newSubSeason + " (ticks: " + newTicks + ")");

                // Se a estação mudou, limpa o cache de cores e força reload do world renderer
                if (oldSubSeason != newSubSeason) {
                    ColorsCache.clear();

                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.worldRenderer != null) {
                        client.worldRenderer.reload();
                    }
                }
            });
        });
    }

    public static Season.SubSeason getSubSeason() {
        return clientSubSeason;
    }

    public static Season getSeason() {
        return clientSubSeason.getSeason();
    }

    public static int getTicks() {
        return clientTicks;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void reset() {
        clientSubSeason = Season.SubSeason.EARLY_SPRING;
        clientTicks = 0;
        initialized = false;
    }
}

