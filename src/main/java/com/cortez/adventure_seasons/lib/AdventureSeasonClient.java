package com.cortez.adventure_seasons.lib;


import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.block.custom.SeasonCalendar;
import com.cortez.adventure_seasons.lib.AdventureSeason;
import com.cortez.adventure_seasons.lib.cache.ColorsCache;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import com.cortez.adventure_seasons.lib.hud.SeasonCalendarTooltipRenderer;
import com.cortez.adventure_seasons.lib.network.SeasonNetworkClient;
import com.cortez.adventure_seasons.lib.resources.FoliageSeasonColors;
import com.cortez.adventure_seasons.lib.resources.GrassSeasonColors;
import com.cortez.adventure_seasons.lib.season.Season;
import com.cortez.adventure_seasons.lib.season.SeasonState;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class AdventureSeasonClient
{
    private static final Map<RegistryKey<World>, Season.SubSeason> lastRenderedSeasonMap = new HashMap<>();

    public void init(){
        // Inicializa o sistema de networking do cliente
        SeasonNetworkClient.init();


        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new GrassSeasonColors());
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new FoliageSeasonColors());

        // Quando o cliente desconecta, limpa o estado sincronizado
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            AdventureSeasons.LOGGER.info("[Adventure Seasons Client] Desconectado do servidor, limpando estado de estação");
            SeasonNetworkClient.reset();
            ColorsCache.clear();
            lastRenderedSeasonMap.clear();
        });

        FabricLoader.getInstance().getModContainer(AdventureSeasons.MODID).ifPresent((container) -> {
            ResourceManagerHelper.registerBuiltinResourcePack(AdventureSeason.identifier("seasonal_lush_caves"), container, Text.literal("Seasonal Lush Caves"), ResourcePackActivationType.DEFAULT_ENABLED);
        });

        SeasonCalendarTooltipRenderer.register();
    }
}
