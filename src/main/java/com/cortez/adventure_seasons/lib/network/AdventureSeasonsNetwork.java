package com.cortez.adventure_seasons.lib.network;


import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class AdventureSeasonsNetwork
{
    public static void registerC2SPackets() {

    }

    public static void registerS2CPackets() {

        // Registra o tipo de payload no cliente e no servidor
        PayloadTypeRegistry.playS2C().register(
                SeasonSyncPayload.ID,
                SeasonSyncPayload.CODEC
        );
    }
}
