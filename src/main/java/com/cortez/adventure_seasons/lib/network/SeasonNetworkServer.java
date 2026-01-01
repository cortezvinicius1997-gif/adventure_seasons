package com.cortez.adventure_seasons.lib.network;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class SeasonNetworkServer {
    private static Season.SubSeason lastSyncedSubSeason = null;

    public static void init() {
        // Registra o tipo de payload no servidor
        PayloadTypeRegistry.playS2C().register(SeasonSyncPayload.ID, SeasonSyncPayload.CODEC);

        // Quando um jogador se conecta, envia a estação atual
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Season.SubSeason currentSubSeason = SeasonState.getSubSeason();
            AdventureSeasons.LOGGER.info("[Adventure Seasons Server] Enviando estação para jogador: " +
                    handler.getPlayer().getName().getString() + " - " + currentSubSeason);

            sender.sendPacket(new SeasonSyncPayload(currentSubSeason));
        });
    }

    /**
     * Sincroniza a estação atual com todos os jogadores conectados.
     * Deve ser chamado sempre que a estação mudar no servidor.
     */
    public static void syncToAllPlayers(MinecraftServer server) {
        Season.SubSeason currentSubSeason = SeasonState.getSubSeason();

        // Só sincroniza se a estação mudou
        if (currentSubSeason == lastSyncedSubSeason) {
            return;
        }

        lastSyncedSubSeason = currentSubSeason;
        SeasonSyncPayload payload = new SeasonSyncPayload(currentSubSeason);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (ServerPlayNetworking.canSend(player, SeasonSyncPayload.ID)) {
                ServerPlayNetworking.send(player, payload);
            }
        }

        AdventureSeasons.LOGGER.info("[Adventure Seasons Server] Estação sincronizada para todos os jogadores: " + currentSubSeason);
    }

    /**
     * Força a sincronização para todos os jogadores, independente de mudança.
     */
    public static void forceSyncToAllPlayers(MinecraftServer server) {
        lastSyncedSubSeason = null;
        syncToAllPlayers(server);
    }

    /**
     * Reseta o estado de sincronização (chamar quando o servidor parar)
     */
    public static void reset() {
        lastSyncedSubSeason = null;
    }
}

