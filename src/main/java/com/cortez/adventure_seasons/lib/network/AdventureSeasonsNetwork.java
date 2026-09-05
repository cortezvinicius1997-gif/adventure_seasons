package com.cortez.adventure_seasons.lib.network;


import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class AdventureSeasonsNetwork
{
    public static void registerC2SPackets() {

    }

    public static void registerS2CPackets() {

        PayloadTypeRegistry.clientboundPlay().register(
                SeasonSyncPayload.ID,
                SeasonSyncPayload.CODEC
        );
    }
}
