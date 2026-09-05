package com.cortez.adventure_seasons.lib.cache;

import com.cortez.adventure_seasons.AdventureSeasons;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

import java.util.IdentityHashMap;
import java.util.Map;

public final class BiomeCache {

    private static final Map<Biome, Identifier> CACHE = new IdentityHashMap<>();
    private static Registry<Biome> biomeRegistry = null;
    private static boolean initialized = false;

    private BiomeCache() {}

    public static void init(ServerLevel world) {
        if (initialized) return;

        biomeRegistry = world.registryAccess().lookupOrThrow(Registries.BIOME);

        for (Map.Entry<ResourceKey<Biome>, Biome> entry : biomeRegistry.entrySet()) {
            Identifier id = entry.getKey().identifier();
            CACHE.put(entry.getValue(), id);
        }

        initialized = true;
        AdventureSeasons.LOGGER.info("[Adventure Mod] Cache de biomas inicializado com " + CACHE.size() + " biomas");
    }

    public static void put(Biome biome, Identifier id) {
        CACHE.put(biome, id);
    }

    public static Identifier get(Biome biome) {
        Identifier id = CACHE.get(biome);

        if (id == null && biomeRegistry != null) {
            id = biomeRegistry.getKey(biome);
            if (id != null) {
                CACHE.put(biome, id);
            }
        }

        return id;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
