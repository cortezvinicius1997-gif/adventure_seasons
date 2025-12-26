package com.cortez.adventure_seasons.lib.cache;

import com.cortez.adventure_seasons.AdventureSeasons;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.IdentityHashMap;
import java.util.Map;

public final class BiomeCache {

    private static final Map<Biome, Identifier> CACHE = new IdentityHashMap<>();
    private static Registry<Biome> biomeRegistry = null;
    private static boolean initialized = false;

    private BiomeCache() {}

    public static void init(ServerWorld world) {
        if (initialized) return;

        biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);

        for (Map.Entry<RegistryKey<Biome>, Biome> entry : biomeRegistry.getEntrySet()) {
            Identifier id = entry.getKey().getValue();
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

        // Se não estiver no cache e o registro estiver disponível, tenta buscar
        if (id == null && biomeRegistry != null) {
            id = biomeRegistry.getId(biome);
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