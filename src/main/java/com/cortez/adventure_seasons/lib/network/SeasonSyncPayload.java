package com.cortez.adventure_seasons.lib.network;

import com.cortez.adventure_seasons.lib.AdventureSeason;
import com.cortez.adventure_seasons.lib.season.Season;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SeasonSyncPayload(String subSeasonName, int ticksInSubSeason) implements CustomPayload {
    public static final Id<SeasonSyncPayload> ID = new Id<>(AdventureSeason.identifier("season_sync"));

    public static final PacketCodec<RegistryByteBuf, SeasonSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, SeasonSyncPayload::subSeasonName,
            PacketCodecs.INTEGER, SeasonSyncPayload::ticksInSubSeason,
            SeasonSyncPayload::new
    );

    public SeasonSyncPayload(Season.SubSeason subSeason, int ticks) {
        this(subSeason.name(), ticks);
    }

    public Season.SubSeason getSubSeason() {
        try {
            return Season.SubSeason.valueOf(subSeasonName);
        } catch (IllegalArgumentException e) {
            return Season.SubSeason.EARLY_SPRING;
        }
    }

    public int getTicks() {
        return ticksInSubSeason;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

