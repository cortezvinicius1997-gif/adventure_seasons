package com.cortez.adventure_seasons.lib;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.lib.cache.BiomeCache;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import com.cortez.adventure_seasons.lib.mixed.BiomeMixed;
import com.cortez.adventure_seasons.lib.network.SeasonNetworkServer;
import com.cortez.adventure_seasons.lib.season.*;
import com.cortez.adventure_seasons.lib.util.PlacedMeltablesState;
import com.cortez.adventure_seasons.lib.util.ReplacedMeltablesState;
import com.cortez.adventure_seasons.lib.util.BiomeAccessor;
import com.cortez.adventure_seasons.mixin.BiomeWeatherAccessor;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.saveddata.WeatherData;

import java.util.Map;


public class AdventureSeason
{
    private static final LongArraySet temporaryMeltableCache = new LongArraySet();
    private SeasonState seasonState;
    private int ticksPerSubSeason;
    private boolean serverStopping = false;
    // Guarda o último dayTime conhecido do overworld para sincronizar o avanço
    // da season com o relógio real do servidor (incluindo saltos causados pelo sono).
    private long lastKnownDayTime = -1L;
    private static final TagKey<Biome> IGNORED_CATEGORIES_TAG = TagKey.create(Registries.BIOME, AdventureSeason.identifier("ignored"));

    public void init(MinecraftServer server, boolean serverStopping){
        this.serverStopping = serverStopping;

        SeasonNetworkServer.init();



        Season.SubSeason startingSubSeason = AdventureSeasonConfig.getStartingSubSeason();

        ticksPerSubSeason = AdventureSeasonConfig.getTicksForSubSeason(startingSubSeason);

        AdventureSeasons.LOGGER.info("[Adventure Mod] Duração de cada subestação: " +
                ticksPerSubSeason + " ticks (" +
                (ticksPerSubSeason / 24000.0) + " dias in-game)");

        EntitySleepEvents.STOP_SLEEPING.register(this::onPlayerWakeUp);

        ServerLevel world = server.overworld();
        BiomeCache.init(world);

        // Sincroniza o marcador de tempo com o dayTime atual do overworld,
        // para que o próximo tick não interprete o tempo já passado como um salto.
        lastKnownDayTime = world.getOverworldClockTime();

        seasonState = SeasonState.getOrCreate(server);

        // Só define a estação inicial se for um mundo novo (nunca teve estação salva antes)
        // Verifica se a subestação é EARLY_SPRING E os ticks são 0 (indicando mundo novo)
        boolean isNewWorld = seasonState.getCurrentSubSeason() == Season.SubSeason.EARLY_SPRING &&
                seasonState.getTicksInCurrentSubSeason() == 0;

        if (isNewWorld && startingSubSeason != Season.SubSeason.EARLY_SPRING) {
            seasonState.setCurrentSubSeason(startingSubSeason);
            AdventureSeasons.LOGGER.info("[Adventure Seasons] Mundo novo detectado! Definindo subestação inicial: " + startingSubSeason);
        }

        // Atualiza o ticksPerSubSeason baseado na estação atual carregada
        ticksPerSubSeason = AdventureSeasonConfig.getTicksForSubSeason(seasonState.getCurrentSubSeason());

        AdventureSeasons.LOGGER.info("[Adventure Seasons] Mod inicializado!");
        AdventureSeasons.LOGGER.info("[Adventure Seasons] Subestação atual: " + seasonState.getCurrentSubSeason());
        AdventureSeasons.LOGGER.info("[Adventure Seasons] Estação atual: " + seasonState.getCurrentSeason());
        AdventureSeasons.LOGGER.info("[Adventure Seasons] Ticks na subestação: " + seasonState.getTicksInCurrentSubSeason());

        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            this.serverStopping = true;

            if (seasonState != null) {
                AdventureSeasons.LOGGER.info("[Adventure Seasons] Salvando estado da estação: " +
                        seasonState.getCurrentSubSeason() + " (Ticks: " + seasonState.getTicksInCurrentSubSeason() + ")");
                seasonState.setDirty();

                // Força o salvamento imediato
                ServerLevel overworld = minecraftServer.getLevel(Level.OVERWORLD);
                if (overworld != null) {
                    overworld.getDataStorage().saveAndJoin();
                }
            }

            // Limpa instância estática para evitar problemas em reload
            SeasonState.clearInstance();
            SeasonNetworkServer.reset();
        });
    }

    private void onPlayerWakeUp(LivingEntity entity, BlockPos blockPos) {
        // Não mexemos mais no seasonState diretamente aqui: quando o jogador dorme,
        // o vanilla avança o dayTime do overworld direto para a manhã. Essa mudança
        // de dayTime é detectada e aplicada de forma unificada no próximo tick()
        // (mesmo mecanismo usado para a passagem normal do tempo), então a season
        // fica sempre sincronizada com o horário real do servidor sem contagem duplicada.
        if (entity instanceof ServerPlayer player) {
            AdventureSeasons.LOGGER.info("[Adventure Seasons] Jogador " + player.getName().getString() +
                    " dormiu. O avanço da subestação será sincronizado com o novo horário do servidor.");
        }
    }

    public void tick(MinecraftServer server)
    {

        if (seasonState == null || serverStopping) {
            return;
        }

        ServerLevel world = server.overworld();

        // Sincroniza o avanço da season com o dayTime real do overworld.
        // Em ticks normais, o delta é 1 (mesmo comportamento de antes).
        // Quando alguém dorme, o vanilla pula o dayTime direto para a manhã,
        // então o delta reflete exatamente esse salto — sem precisar de um
        // valor fixo de 24000 tratado separadamente no evento de dormir.
        long currentDayTime = world.getOverworldClockTime();

        if (lastKnownDayTime < 0) {
            lastKnownDayTime = currentDayTime;
        }

        long elapsedDayTime = currentDayTime - lastKnownDayTime;

        // Proteção contra retrocesso de tempo (ex.: comando /time set para trás)
        // para não fazer a season "andar para trás".
        if (elapsedDayTime < 0) {
            elapsedDayTime = 0;
        }

        lastKnownDayTime = currentDayTime;

        if (elapsedDayTime > 0) {
            seasonState.addTicks((int) Math.min(elapsedDayTime, Integer.MAX_VALUE));
        }

        Season.SubSeason subSeason = seasonState.getCurrentSubSeason();

        if(seasonState.getCurrentSeason() == Season.WINTER && subSeason == Season.SubSeason.MID_WINTER){
            WinterVegetationManager.tick(world, seasonState.getCurrentSeason());
        }



        // Chuva constante no inverno (se habilitado)
        if (AdventureSeasonConfig.isWinterRain()) {
            if (seasonState.getCurrentSeason() == Season.WINTER) {
                if (subSeason == Season.SubSeason.MID_WINTER) {
                    WeatherData weatherData = world.getWeatherData();
                    weatherData.setClearWeatherTime(0);
                    weatherData.setRainTime(Integer.MAX_VALUE);
                    weatherData.setRaining(true);
                    weatherData.setThundering(false);
                }else if(subSeason == Season.SubSeason.LATE_WINTER){
                    WeatherData weatherData = world.getWeatherData();
                    weatherData.setClearWeatherTime(0);
                    weatherData.setRainTime(0);
                    weatherData.setRaining(false);
                    weatherData.setThundering(false);
                }

            }
        }

        if (seasonState.getCurrentSeason() == Season.SPRING){
            if (subSeason == Season.SubSeason.MID_SPRING){
                SpringVegetationManager.tick(world, seasonState.getCurrentSeason());
            }
        }

        if (seasonState.getTicksInCurrentSubSeason() >= ticksPerSubSeason) {
            Season.SubSeason oldSubSeason = seasonState.getCurrentSubSeason();
            seasonState.nextSubSeason();

            Season.SubSeason newSubSeason = seasonState.getCurrentSubSeason();

            server.getPlayerList().broadcastSystemMessage(
                    Component.translatable(
                            "message.adventure_season.server",
                            seasonState.getCurrentSeason().getDisplayName(),
                            seasonState.getCurrentSubSeason().getDisplayName()
                    ),
                    false
            );

            ticksPerSubSeason = AdventureSeasonConfig.getTicksForSubSeason(newSubSeason);

            AdventureSeasons.LOGGER.info("[Adventure Mod] Subestação mudou de " + oldSubSeason + " para " + newSubSeason);

            // ADICIONE ESTAS LINHAS:
            CropGrowthManager.logSeasonChange(newSubSeason);

            String growthInfo = CropGrowthManager.getGrowthDescription(newSubSeason);
            server.getPlayerList().broadcastSystemMessage(
                    Component.literal("§e🌾 §f" + growthInfo),
                    false
            );

            updateAllSeasonSensors(world);

            // Notifica jogadores sobre o derretimento natural
            if (seasonState.getCurrentSeason() != Season.WINTER && seasonState.getCurrentSubSeason() == Season.SubSeason.EARLY_SPRING) {
                server.getPlayerList().broadcastSystemMessage(
                        Component.literal("§b❄ §fA neve começará a derreter naturalmente..."),
                        false
                );
            }

            // Sincroniza a nova estação com todos os clientes
            SeasonNetworkServer.syncToAllPlayers(server);
        }

        // Sincronização periódica para garantir que todos os clientes estejam atualizados
        SeasonNetworkServer.syncToAllPlayers(server);

        updatePlayerActionBar(server);
    }



    public static void injectBiomeTemperature(Holder<Biome> entry, Level world)
    {
        if(entry.is(IGNORED_CATEGORIES_TAG))
            return;

        Biome biome = entry.value();
        Identifier biomeId = entry.unwrapKey().orElse(Biomes.PLAINS).identifier();

        if (AdventureSeasonConfig.isExcludedBiome(biomeId))
            return;

        if(!AdventureSeasonConfig.doTemperatureChanges(biomeId)) return;

        var currentWeather = BiomeAccessor.getClimateSettings(biome);
        BiomeMixed mixed = (BiomeMixed) (Object) biome;

        BiomeWeatherAccessor weatherAccessor = (BiomeWeatherAccessor) (Object) currentWeather;

        if (mixed.getOriginalTemperatureModifier() == null) {
            mixed.setOriginalTemperature(weatherAccessor.getTemperature());
            mixed.setOriginalHasPrecipitation(weatherAccessor.getHasPrecipitation());
            mixed.setOriginalDownfall(weatherAccessor.getDownfall());
            mixed.setOriginalTemperatureModifier(weatherAccessor.getTemperatureModifier());
        }

        // Usa dados sincronizados do servidor em multiplayer (client-side)
        Season.SubSeason subSeason = getClientOrServerSubSeason();

        Map.Entry<Boolean, Float> modifiedWeather = getSeasonWeather(subSeason, biomeId, mixed.getOriginalHasPrecipitation(), mixed.getOriginalTemperature());

        // Usa accessor para modificar campos final do ClimateSettings
        weatherAccessor.setHasPrecipitation(modifiedWeather.getKey());
        weatherAccessor.setTemperature(modifiedWeather.getValue());
    }

    /**
     * Obtém a subestação atual, usando dados sincronizados do servidor em multiplayer
     */
    private static Season.SubSeason getClientOrServerSubSeason() {
        try {
            // Tenta usar dados do cliente sincronizados (funciona em multiplayer)
            Class<?> networkClientClass = Class.forName("com.cortez.adventure_seasons.lib.network.SeasonNetworkClient");
            Boolean isInitialized = (Boolean) networkClientClass.getMethod("isInitialized").invoke(null);
            if (isInitialized) {
                return (Season.SubSeason) networkClientClass.getMethod("getSubSeason").invoke(null);
            }
        } catch (Exception ignored) {
            // Em servidor dedicado ou se a classe não existir, usa SeasonState diretamente
        }
        return SeasonState.getSubSeason();
    }

    private static Map.Entry<Boolean, Float> getSeasonWeather(Season.SubSeason subSeason, Identifier biomeId, boolean hasPrecipitation, float temperature)
    {
        Season season = subSeason.getSeason();

        if(!AdventureSeasonConfig.doTemperatureChanges(biomeId)) {
            return Map.entry(hasPrecipitation, temperature);
        }

        if(AdventureSeasonConfig.isSnowForcedInBiome(biomeId) && season == Season.WINTER) {
            float tempModifier = getTemperatureModifierForSubSeason(subSeason, temperature);
            return Map.entry(hasPrecipitation, tempModifier);
        }

        // Calcula modificadores baseados na subestação
        float tempModifier = getTemperatureModifierForSubSeason(subSeason, temperature);
        float finalTemperature = temperature + tempModifier;

        // No inverno, força a temperatura para garantir neve em todos os biomas
        // Para nevar, a temperatura precisa ser <= 0.15
        if (season == Season.WINTER) {
            // Garante que a temperatura final seja baixa o suficiente para nevar
            float maxTempForSnow = 0.15f;
            if (finalTemperature > maxTempForSnow) {
                // Se mesmo com o modificador a temperatura ainda está alta demais, força para nevar
                finalTemperature = maxTempForSnow - 0.1f; // Um pouco abaixo do limite para garantir neve
            }
            // Habilita precipitação no inverno
            return Map.entry(true, finalTemperature);
        }

        if(temperature <= -0.51) {
            // Permanently Frozen Biomes
            return Map.entry(hasPrecipitation, finalTemperature);
        } else if(temperature <= 0.15) {
            // Usually Frozen Biomes
            if (season == Season.SUMMER && !AdventureSeasonConfig.shouldSnowyBiomesMeltInSummer()) {
                return Map.entry(hasPrecipitation, temperature); // Sem modificação
            }
            return Map.entry(hasPrecipitation, finalTemperature);
        } else if(temperature <= 0.49) {
            // Temperate Biomes
            return Map.entry(hasPrecipitation, finalTemperature);
        } else if(temperature <= 0.79) {
            // Usually Ice Free Biomes
            return Map.entry(hasPrecipitation, finalTemperature);
        } else {
            // Ice Free Biomes
            boolean precipitationModified = season == Season.WINTER || hasPrecipitation;
            return Map.entry(precipitationModified, finalTemperature);
        }
    }

    private static float getTemperatureModifierForSubSeason(Season.SubSeason subSeason, float baseTemperature) {
        boolean isReversed = AdventureSeasonConfig.isFallAndSpringReversed();

        return switch (subSeason) {
            // PRIMAVERA
            case EARLY_SPRING -> {
                if (isReversed) {
                    yield getModifierByTemperatureRange(baseTemperature, -0.35f, -0.30f, -0.20f, -0.40f, -0.40f);
                } else {
                    yield getModifierByTemperatureRange(baseTemperature, -0.05f, -0.05f, -0.05f, -0.05f, -0.05f);
                }
            }
            case MID_SPRING -> {
                if (isReversed) {
                    yield getModifierByTemperatureRange(baseTemperature, -0.25f, -0.20f, -0.12f, -0.30f, -0.30f);
                } else {
                    yield getModifierByTemperatureRange(baseTemperature, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f);
                }
            }
            case LATE_SPRING -> {
                if (isReversed) {
                    yield getModifierByTemperatureRange(baseTemperature, -0.15f, -0.10f, -0.06f, -0.20f, -0.20f);
                } else {
                    yield getModifierByTemperatureRange(baseTemperature, 0.15f, 0.10f, 0.10f, 0.15f, 0.15f);
                }
            }

            // VERÃO
            case EARLY_SUMMER -> getModifierByTemperatureRange(baseTemperature, 0.50f, 0.40f, 0.35f, 0.25f, 0.20f);
            case MID_SUMMER -> getModifierByTemperatureRange(baseTemperature, 0.84f, 0.66f, 0.66f, 0.46f, 0.40f);
            case LATE_SUMMER -> getModifierByTemperatureRange(baseTemperature, 0.70f, 0.55f, 0.55f, 0.38f, 0.33f);

            // OUTONO
            case EARLY_AUTUMN -> {
                if (isReversed) {
                    yield getModifierByTemperatureRange(baseTemperature, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f);
                } else {
                    yield getModifierByTemperatureRange(baseTemperature, -0.15f, -0.12f, -0.08f, -0.18f, -0.18f);
                }
            }
            case MID_AUTUMN -> {
                if (isReversed) {
                    yield getModifierByTemperatureRange(baseTemperature, -0.05f, -0.05f, -0.05f, -0.05f, -0.05f);
                } else {
                    yield getModifierByTemperatureRange(baseTemperature, -0.22f, -0.18f, -0.12f, -0.28f, -0.28f);
                }
            }
            case LATE_AUTUMN -> {
                if (isReversed) {
                    yield getModifierByTemperatureRange(baseTemperature, -0.10f, -0.10f, -0.08f, -0.12f, -0.12f);
                } else {
                    yield getModifierByTemperatureRange(baseTemperature, -0.30f, -0.25f, -0.16f, -0.34f, -0.34f);
                }
            }

            // INVERNO - Modificadores mais fortes para garantir neve em todos os biomas
            // Para nevar, a temperatura final deve ser <= 0.15
            // Biomas quentes (temperatura base ~0.8 a 2.0) precisam de modificadores mais fortes
            case EARLY_WINTER -> getModifierByTemperatureRange(baseTemperature, -0.60f, -0.65f, -0.70f, -0.80f, -0.90f);
            case MID_WINTER -> getModifierByTemperatureRange(baseTemperature, -0.70f, -0.75f, -0.80f, -0.90f, -1.0f);
            case LATE_WINTER -> getModifierByTemperatureRange(baseTemperature, -0.50f, -0.55f, -0.60f, -0.70f, -0.80f);
        };
    }

    private static float getModifierByTemperatureRange(float temperature,
                                                       float permFrozen,    // <= -0.51
                                                       float usuallyFrozen, // <= 0.15
                                                       float temperate,     // <= 0.49
                                                       float usuallyFree,   // <= 0.79
                                                       float iceFree)       // > 0.79
    {
        if (temperature <= -0.51) {
            return permFrozen;
        } else if (temperature <= 0.15) {
            return usuallyFrozen;
        } else if (temperature <= 0.49) {
            return temperate;
        } else if (temperature <= 0.79) {
            return usuallyFree;
        } else {
            return iceFree;
        }
    }

    private void updateAllSeasonSensors(ServerLevel world) {

        AdventureSeasons.LOGGER.info("[Adventure Seasons] Season Sensors atualizados para estação: " +
                seasonState.getCurrentSeason());
    }


    private void updateAllSeasonSensorsOptimized(ServerLevel world) {
        // Itera sobre jogadores e força atualização de redstone na área
        for (ServerPlayer player : world.players()) {
            BlockPos playerPos = player.blockPosition();

            // Atualiza em um raio menor mas com saltos maiores
            for (int x = -64; x <= 64; x += 8) {
                for (int z = -64; z <= 64; z += 8) {
                    for (int y = world.getMinY(); y < world.getMaxY(); y += 8) {
                        BlockPos checkPos = playerPos.offset(x, y, z);

                        if (world.isLoaded(checkPos)) {
                            // Força atualização de redstone nesta região
                            world.updateNeighborsAt(checkPos, world.getBlockState(checkPos).getBlock());
                        }
                    }
                }
            }
        }

        AdventureSeasons.LOGGER.info("[Adventure Seasons] Região atualizada para nova estação");
    }

    private void updatePlayerActionBar(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {

            ServerLevel world = player.level();
            Holder<Biome> biomeEntry = world.getBiome(player.blockPosition());

            Identifier biomeId = world.registryAccess()
                    .lookupOrThrow(Registries.BIOME)
                    .getKey(biomeEntry.value());

            String biomeName = biomeId != null ? biomeId.toString() : "desconhecido";
            boolean excluded = AdventureSeasonConfig.isExcludedBiome(biomeId);
            String excludedTag = excluded ? " §c[EXCLUÍDO]" : "";

            int ticksRemaining = ticksPerSubSeason - seasonState.getTicksInCurrentSubSeason();
            int secondsRemaining = ticksRemaining / 20;

            if (AdventureSeasonConfig.isDebug()) {
                player.sendSystemMessage(
                        Component.translatable(
                                "debug.adventure_season.info",
                                seasonState.getCurrentSeason().getDisplayName(),
                                seasonState.getCurrentSubSeason().getDisplayName(),
                                secondsRemaining,
                                biomeName,
                                excludedTag,
                                String.format("%.2f", biomeEntry.value().getBaseTemperature())
                        ),
                        true
                );

            }
        }
    }

    /*private static void debugRain(ServerPlayer player) {
        ServerLevel world = player.level();

        if (!world.isRaining()) {
            return;
        }

        BlockPos pos = player.blockPosition();

        boolean skyVisible = world.isSkyVisible(pos);
        boolean isRainBiome = world.getBiome(pos).value().getPrecipitation(pos) == Biome.Precipitation.RAIN;
        boolean isSnowBiome = world.getBiome(pos).value().getPrecipitation(pos) == Biome.Precipitation.SNOW;

        if (skyVisible && isRainBiome || isSnowBiome) {
            player.sendMessage(
                    Component.literal("Está pegando chuva"),
                    true
            );
        } else {
            player.sendMessage(
                    Component.literal("Não está pegando chuva"),
                    true
            );
        }
    }*/

    public static void setMeltable(BlockPos blockPos) {
        temporaryMeltableCache.add(blockPos.asLong());
    }

    public static boolean isMeltable(BlockPos blockPos) {
        return temporaryMeltableCache.contains(blockPos.asLong());
    }

    public static PlacedMeltablesState getPlacedMeltablesState(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(PlacedMeltablesState.getPersistentStateType());
    }

    public static ReplacedMeltablesState getReplacedMeltablesState(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(ReplacedMeltablesState.getPersistentStateType());
    }

    public static Identifier identifier(String path)
    {
        return Identifier.fromNamespaceAndPath(AdventureSeasons.MODID, path);
    }
}
