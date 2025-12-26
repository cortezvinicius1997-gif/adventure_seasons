package com.cortez.adventure_seasons.lib;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.block.custom.SeasonSensor;
import com.cortez.adventure_seasons.lib.cache.BiomeCache;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import com.cortez.adventure_seasons.lib.mixed.BiomeMixed;
import com.cortez.adventure_seasons.lib.season.*;
import com.cortez.adventure_seasons.lib.util.PlacedMeltablesState;
import com.cortez.adventure_seasons.lib.util.ReplacedMeltablesState;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.ArrayList;
import java.util.List;

public class AdventureSeason
{
    private static final LongArraySet temporaryMeltableCache = new LongArraySet();
    private SeasonState seasonState;
    private int ticksPerSubSeason;
    private boolean serverStopping = false;
    private static final TagKey<Biome> IGNORED_CATEGORIES_TAG = TagKey.of(RegistryKeys.BIOME, AdventureSeason.identifier("ignored"));

    public void init(MinecraftServer server, boolean serverStopping){
        this.serverStopping = serverStopping;

        Season.SubSeason startingSubSeason = AdventureSeasonConfig.getStartingSubSeason();

        ticksPerSubSeason = AdventureSeasonConfig.getTicksForSubSeason(startingSubSeason);

        AdventureSeasons.LOGGER.info("[Adventure Mod] Duração de cada subestação: " +
                ticksPerSubSeason + " ticks (" +
                (ticksPerSubSeason / 24000.0) + " dias in-game)");

        EntitySleepEvents.STOP_SLEEPING.register(this::onPlayerWakeUp);

        ServerWorld world = server.getOverworld();
        BiomeCache.init(world);

        seasonState = SeasonState.getOrCreate(server);

        if (seasonState.getCurrentSubSeason() == Season.SubSeason.EARLY_SPRING &&
                seasonState.getTicksInCurrentSubSeason() == 0) {

            if (startingSubSeason != Season.SubSeason.EARLY_SPRING) {
                seasonState.setCurrentSubSeason(startingSubSeason);
                AdventureSeasons.LOGGER.info("[Adventure Seasons] Definindo subestação inicial: " + startingSubSeason);
            }
        }

        AdventureSeasons.LOGGER.info("[Adventure Seasons] Mod inicializado!");
        AdventureSeasons.LOGGER.info("[Adventure Seasons] Subestação atual: " + seasonState.getCurrentSubSeason());
        AdventureSeasons.LOGGER.info("[Adventure Seasons] Estação atual: " + seasonState.getCurrentSeason());

        ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> {
            this.serverStopping = true;

            if (seasonState != null) {
                AdventureSeasons.LOGGER.info("[Adventure Seasons] Salvando estado da estação...");
                seasonState.markDirty();
            }
        });
    }

    private void onPlayerWakeUp(LivingEntity entity, BlockPos blockPos) {
        if (entity instanceof ServerPlayerEntity player && seasonState != null) {
            seasonState.addTicks(24000);

            AdventureSeasons.LOGGER.info("[Adventure Seasons] Jogador " + player.getName().getString() +
                    " dormiu. Avançando 1 dia na subestação.");

            if (seasonState.getTicksInCurrentSubSeason() >= ticksPerSubSeason) {
                Season.SubSeason oldSubSeason = seasonState.getCurrentSubSeason();
                seasonState.nextSubSeason();
                Season.SubSeason newSubSeason = seasonState.getCurrentSubSeason();

                ticksPerSubSeason = AdventureSeasonConfig.getTicksForSubSeason(newSubSeason);

                CropGrowthManager.logSeasonChange(newSubSeason);

                String growthInfo = CropGrowthManager.getGrowthDescription(newSubSeason);



                player.getServer().getPlayerManager().broadcast(
                        Text.translatable(
                                "message.adventure_season.server",
                                seasonState.getCurrentSeason().getDisplayName(),
                                seasonState.getCurrentSubSeason().getDisplayName()
                        ),
                        false
                );

                player.getServer().getPlayerManager().broadcast(
                        Text.literal("§e🌾 §f" + growthInfo),
                        false
                );
            }
        }
    }

    public void tick(MinecraftServer server)
    {

        if (seasonState == null || serverStopping) {
            return;
        }

        seasonState.incrementTicks();

        ServerWorld world = server.getOverworld();

        Season.SubSeason subSeason = seasonState.getCurrentSubSeason();

        if(seasonState.getCurrentSeason() == Season.WINTER && subSeason == Season.SubSeason.MID_WINTER){
            WinterVegetationManager.tick(world, seasonState.getCurrentSeason());
        }



        // Chuva constante no inverno (se habilitado)
        if (AdventureSeasonConfig.isWinterRain()) {
            if (seasonState.getCurrentSeason() == Season.WINTER) {
                if (subSeason == Season.SubSeason.MID_WINTER) {
                    world.setWeather(0, Integer.MAX_VALUE, true, false);
                }else if(subSeason == Season.SubSeason.LATE_WINTER){
                    world.setWeather(0, 0, false, false);
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

            server.getPlayerManager().broadcast(
                    Text.translatable(
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
            server.getPlayerManager().broadcast(
                    Text.literal("§e🌾 §f" + growthInfo),
                    false
            );

            updateAllSeasonSensors(world);

            // Notifica jogadores sobre o derretimento natural
            if (seasonState.getCurrentSeason() != Season.WINTER) {
                server.getPlayerManager().broadcast(
                        Text.literal("§b❄ §fA neve começará a derreter naturalmente..."),
                        false
                );
            }
        }
        updatePlayerActionBar(server);
    }



    public static void injectBiomeTemperature(RegistryEntry<Biome> entry, World world)
    {
        if(entry.isIn(IGNORED_CATEGORIES_TAG))
            return;

        Biome biome = entry.value();
        Identifier biomeId = entry.getKey().orElse(BiomeKeys.PLAINS).getValue();

        if (AdventureSeasonConfig.isExcludedBiome(biomeId))
            return;

        if(!AdventureSeasonConfig.doTemperatureChanges(biomeId)) return;

        Biome.Weather currentWeather = biome.weather;
        Biome.Weather originalWeather = ((BiomeMixed) (Object) biome).getOriginalWeather();
        if (originalWeather == null) {
            originalWeather = new Biome.Weather(currentWeather.hasPrecipitation(), currentWeather.temperature(), currentWeather.temperatureModifier(), currentWeather.downfall());
            ((BiomeMixed) (Object) biome).setOriginalWeather(originalWeather);
        }

        Season.SubSeason subSeason = SeasonState.getSubSeason();

        Pair<Boolean, Float> modifiedWeather = getSeasonWeather(subSeason, biomeId, originalWeather.hasPrecipitation, originalWeather.temperature);
        currentWeather.hasPrecipitation = modifiedWeather.getLeft();
        currentWeather.temperature = modifiedWeather.getRight();
    }

    private static Pair<Boolean, Float> getSeasonWeather(Season.SubSeason subSeason, Identifier biomeId, boolean hasPrecipitation, float temperature)
    {
        Season season = subSeason.getSeason();

        if(!AdventureSeasonConfig.doTemperatureChanges(biomeId)) {
            return new Pair<>(hasPrecipitation, temperature);
        }

        if(AdventureSeasonConfig.isSnowForcedInBiome(biomeId) && season == Season.WINTER) {
            float tempModifier = getTemperatureModifierForSubSeason(subSeason, temperature);
            return new Pair<>(hasPrecipitation, tempModifier);
        }

        // Calcula modificadores baseados na subestação
        float tempModifier = getTemperatureModifierForSubSeason(subSeason, temperature);

        if(temperature <= -0.51) {
            // Permanently Frozen Biomes
            return new Pair<>(hasPrecipitation, temperature + tempModifier);
        } else if(temperature <= 0.15) {
            // Usually Frozen Biomes
            float modifier = tempModifier;
            if (season == Season.SUMMER && !AdventureSeasonConfig.shouldSnowyBiomesMeltInSummer()) {
                modifier = 0f;
            }
            return new Pair<>(hasPrecipitation, temperature + modifier);
        } else if(temperature <= 0.49) {
            // Temperate Biomes
            return new Pair<>(hasPrecipitation, temperature + tempModifier);
        } else if(temperature <= 0.79) {
            // Usually Ice Free Biomes
            return new Pair<>(hasPrecipitation, temperature + tempModifier);
        } else {
            // Ice Free Biomes
            boolean precipitationModified = season == Season.WINTER ? true : hasPrecipitation;
            return new Pair<>(precipitationModified, temperature + tempModifier);
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

            // INVERNO
            case EARLY_WINTER -> getModifierByTemperatureRange(baseTemperature, -0.60f, -0.65f, -0.70f, -0.48f, -0.55f);
            case MID_WINTER -> getModifierByTemperatureRange(baseTemperature, -0.70f, -0.75f, -0.80f, -0.56f, -0.64f);
            case LATE_WINTER -> getModifierByTemperatureRange(baseTemperature, -0.50f, -0.55f, -0.60f, -0.40f, -0.48f);
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

    private void updateAllSeasonSensors(ServerWorld world) {

        AdventureSeasons.LOGGER.info("[Adventure Seasons] Season Sensors atualizados para estação: " +
                seasonState.getCurrentSeason());
    }


    private void updateAllSeasonSensorsOptimized(ServerWorld world) {
        // Itera sobre jogadores e força atualização de redstone na área
        for (ServerPlayerEntity player : world.getPlayers()) {
            BlockPos playerPos = player.getBlockPos();

            // Atualiza em um raio menor mas com saltos maiores
            for (int x = -64; x <= 64; x += 8) {
                for (int z = -64; z <= 64; z += 8) {
                    for (int y = world.getBottomY(); y < world.getTopY(); y += 8) {
                        BlockPos checkPos = playerPos.add(x, y, z);

                        if (world.isChunkLoaded(checkPos)) {
                            // Força atualização de redstone nesta região
                            world.updateNeighborsAlways(checkPos, world.getBlockState(checkPos).getBlock());
                        }
                    }
                }
            }
        }

        AdventureSeasons.LOGGER.info("[Adventure Seasons] Região atualizada para nova estação");
    }

    private void updatePlayerActionBar(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            ServerWorld world = (ServerWorld) player.getWorld();
            RegistryEntry<Biome> biomeEntry = world.getBiome(player.getBlockPos());

            Identifier biomeId = world.getRegistryManager()
                    .get(RegistryKeys.BIOME)
                    .getId(biomeEntry.value());

            String biomeName = biomeId != null ? biomeId.toString() : "desconhecido";
            boolean excluded = biomeId != null && AdventureSeasonConfig.isExcludedBiome(biomeId);
            String excludedTag = excluded ? " §c[EXCLUÍDO]" : "";

            int ticksRemaining = ticksPerSubSeason - seasonState.getTicksInCurrentSubSeason();
            int secondsRemaining = ticksRemaining / 20;

            if (AdventureSeasonConfig.isDebug()) {
                player.sendMessage(
                        Text.translatable(
                                "debug.adventure_season.info",
                                seasonState.getCurrentSeason().getDisplayName(),
                                seasonState.getCurrentSubSeason().getDisplayName(),
                                secondsRemaining,
                                biomeName,
                                excludedTag,
                                String.format("%.2f", biomeEntry.value().getTemperature())
                        ),
                        true
                );

            }
        }
    }

    /*private static void debugRain(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        if (!world.isRaining()) {
            return;
        }

        BlockPos pos = player.getBlockPos();

        boolean skyVisible = world.isSkyVisible(pos);
        boolean isRainBiome = world.getBiome(pos).value().getPrecipitation(pos) == Biome.Precipitation.RAIN;
        boolean isSnowBiome = world.getBiome(pos).value().getPrecipitation(pos) == Biome.Precipitation.SNOW;

        if (skyVisible && isRainBiome || isSnowBiome) {
            player.sendMessage(
                    Text.literal("Está pegando chuva"),
                    true
            );
        } else {
            player.sendMessage(
                    Text.literal("Não está pegando chuva"),
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

    public static PlacedMeltablesState getPlacedMeltablesState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(PlacedMeltablesState.getPersistentStateType(), "seasons_placed_meltables");
    }

    public static ReplacedMeltablesState getReplacedMeltablesState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(ReplacedMeltablesState.getPersistentStateType(), "seasons_replaced_meltables");
    }

    public static Identifier identifier(String path)
    {
        return Identifier.of(AdventureSeasons.MODID, path);
    }
}