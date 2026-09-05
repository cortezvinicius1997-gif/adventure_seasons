package com.cortez.adventure_seasons.lib.season;

import com.cortez.adventure_seasons.lib.cache.BiomeCache;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.*;

public class SpringVegetationManager {

    private static final RandomSource RANDOM = RandomSource.create();
    private static int tickCounter = 0;

    // Configurações de crescimento de vegetação
    private static final int CHECK_INTERVAL = 5;
    private static final int CHUNKS_PER_TICK = 8;
    private static final int BLOCKS_PER_CHUNK = 16;
    private static final double SPAWN_CHANCE = 0.15;
    private static final double FLOWER_CHANCE = 0.03;
    // SAPLING_CHANCE e MAX_SAPLINGS_PER_CHUNK agora vêm da config
    // (AdventureSeasonConfig.getSpringSaplingSpawnChance() / getMaxSaplingsPerChunk()),
    // permitindo ao jogador ajustar ou desligar o nascimento automático de árvores.

    // Controle de saplings por chunk
    private static final Map<ChunkPos, Integer> chunkSaplingCount = new HashMap<>();

    // Mapeamento de biomas para vegetação apropriada
    private static final Map<String, List<Block>> BIOME_VEGETATION = new HashMap<>();

    // Mapeamento de biomas para saplings apropriadas
    private static final Map<String, List<Block>> BIOME_SAPLINGS = new HashMap<>();

    static {
        // Plains (Planícies)
        List<Block> plainsVegetation = Arrays.asList(
                Blocks.SHORT_GRASS, Blocks.TALL_GRASS,
                Blocks.DANDELION, Blocks.POPPY,
                Blocks.CORNFLOWER, Blocks.AZURE_BLUET
        );
        BIOME_VEGETATION.put("plains", plainsVegetation);
        BIOME_VEGETATION.put("sunflower_plains", plainsVegetation);

        // Forest (Florestas)
        List<Block> forestVegetation = Arrays.asList(
                Blocks.SHORT_GRASS, Blocks.TALL_GRASS,
                Blocks.FERN, Blocks.LARGE_FERN,
                Blocks.DANDELION, Blocks.POPPY,
                Blocks.LILY_OF_THE_VALLEY
        );
        BIOME_VEGETATION.put("forest", forestVegetation);
        BIOME_VEGETATION.put("birch_forest", forestVegetation);
        BIOME_VEGETATION.put("dark_forest", forestVegetation);
        BIOME_VEGETATION.put("flower_forest", Arrays.asList(
                Blocks.SHORT_GRASS, Blocks.TALL_GRASS,
                Blocks.DANDELION, Blocks.POPPY, Blocks.ALLIUM,
                Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
                Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY,
                Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY,
                Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY
        ));

        // Taiga
        List<Block> taigaVegetation = Arrays.asList(
                Blocks.SHORT_GRASS, Blocks.FERN,
                Blocks.LARGE_FERN, Blocks.SWEET_BERRY_BUSH
        );
        BIOME_VEGETATION.put("taiga", taigaVegetation);
        BIOME_VEGETATION.put("snowy_taiga", taigaVegetation);
        BIOME_VEGETATION.put("old_growth_pine_taiga", taigaVegetation);
        BIOME_VEGETATION.put("old_growth_spruce_taiga", taigaVegetation);

        // Mountains (Montanhas)
        List<Block> mountainVegetation = Arrays.asList(
                Blocks.SHORT_GRASS, Blocks.DANDELION
        );
        BIOME_VEGETATION.put("windswept_hills", mountainVegetation);
        BIOME_VEGETATION.put("windswept_forest", mountainVegetation);
        BIOME_VEGETATION.put("windswept_gravelly_hills", mountainVegetation);

        // Meadow (Prado)
        BIOME_VEGETATION.put("meadow", Arrays.asList(
                Blocks.SHORT_GRASS, Blocks.TALL_GRASS,
                Blocks.DANDELION, Blocks.POPPY, Blocks.ALLIUM,
                Blocks.AZURE_BLUET, Blocks.CORNFLOWER
        ));

        // Swamp (Pântano)
        BIOME_VEGETATION.put("swamp", Arrays.asList(
                Blocks.SHORT_GRASS, Blocks.TALL_GRASS,
                Blocks.BLUE_ORCHID
        ));
        BIOME_VEGETATION.put("mangrove_swamp", Arrays.asList(
                Blocks.SHORT_GRASS, Blocks.TALL_GRASS
        ));

        // Savanna
        List<Block> savannaVegetation = Arrays.asList(
                Blocks.SHORT_GRASS, Blocks.TALL_GRASS
        );
        BIOME_VEGETATION.put("savanna", savannaVegetation);
        BIOME_VEGETATION.put("savanna_plateau", savannaVegetation);
        BIOME_VEGETATION.put("windswept_savanna", savannaVegetation);

        // Desert (Deserto) - vegetação mínima
        BIOME_VEGETATION.put("desert", Arrays.asList(Blocks.DEAD_BUSH));

        // Vegetação padrão para biomas não mapeados
        BIOME_VEGETATION.put("default", Arrays.asList(
                Blocks.SHORT_GRASS, Blocks.TALL_GRASS,
                Blocks.DANDELION, Blocks.POPPY
        ));

        // ===== SAPLINGS POR BIOMA =====

        // Plains (Planícies) - Oak
        List<Block> plainsSaplings = Arrays.asList(Blocks.OAK_SAPLING);
        BIOME_SAPLINGS.put("plains", plainsSaplings);
        BIOME_SAPLINGS.put("sunflower_plains", plainsSaplings);

        // Forest (Florestas) - Oak e Birch
        List<Block> forestSaplings = Arrays.asList(
                Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING
        );
        BIOME_SAPLINGS.put("forest", forestSaplings);
        BIOME_SAPLINGS.put("birch_forest", Arrays.asList(Blocks.BIRCH_SAPLING));
        BIOME_SAPLINGS.put("dark_forest", Arrays.asList(
                Blocks.DARK_OAK_SAPLING, Blocks.OAK_SAPLING
        ));
        BIOME_SAPLINGS.put("flower_forest", forestSaplings);

        // Taiga - Spruce
        List<Block> taigaSaplings = Arrays.asList(Blocks.SPRUCE_SAPLING);
        BIOME_SAPLINGS.put("taiga", taigaSaplings);
        BIOME_SAPLINGS.put("snowy_taiga", taigaSaplings);
        BIOME_SAPLINGS.put("old_growth_pine_taiga", taigaSaplings);
        BIOME_SAPLINGS.put("old_growth_spruce_taiga", taigaSaplings);

        // Mountains - Spruce principalmente
        BIOME_SAPLINGS.put("windswept_hills", Arrays.asList(
                Blocks.SPRUCE_SAPLING, Blocks.OAK_SAPLING
        ));
        BIOME_SAPLINGS.put("windswept_forest", Arrays.asList(
                Blocks.SPRUCE_SAPLING, Blocks.OAK_SAPLING
        ));
        BIOME_SAPLINGS.put("windswept_gravelly_hills", Arrays.asList(Blocks.SPRUCE_SAPLING));

        // Meadow - Oak e Birch
        BIOME_SAPLINGS.put("meadow", Arrays.asList(
                Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING
        ));

        // Swamp - Oak principalmente
        BIOME_SAPLINGS.put("swamp", Arrays.asList(Blocks.OAK_SAPLING));
        BIOME_SAPLINGS.put("mangrove_swamp", Arrays.asList(Blocks.MANGROVE_PROPAGULE));

        // Savanna - Acacia
        List<Block> savannaSaplings = Arrays.asList(Blocks.ACACIA_SAPLING);
        BIOME_SAPLINGS.put("savanna", savannaSaplings);
        BIOME_SAPLINGS.put("savanna_plateau", savannaSaplings);
        BIOME_SAPLINGS.put("windswept_savanna", savannaSaplings);

        // Jungle - Jungle trees
        List<Block> jungleSaplings = Arrays.asList(Blocks.JUNGLE_SAPLING);
        BIOME_SAPLINGS.put("jungle", jungleSaplings);
        BIOME_SAPLINGS.put("bamboo_jungle", jungleSaplings);
        BIOME_SAPLINGS.put("sparse_jungle", jungleSaplings);

        // Cherry Grove
        BIOME_SAPLINGS.put("cherry_grove", Arrays.asList(Blocks.CHERRY_SAPLING));

        // Desert - sem saplings (muito seco)
        BIOME_SAPLINGS.put("desert", new ArrayList<>());

        // Sapling padrão
        BIOME_SAPLINGS.put("default", Arrays.asList(Blocks.OAK_SAPLING));
    }

    /**
     * Chamado a cada tick do servidor para fazer vegetação crescer na primavera
     */
    public static void tick(ServerLevel world, Season currentSeason) {
        if (currentSeason != Season.SPRING) {
            if (!chunkSaplingCount.isEmpty()) {
                chunkSaplingCount.clear();
            }
            return;
        }

        tickCounter++;

        if (tickCounter < CHECK_INTERVAL) {
            return;
        }

        tickCounter = 0;

        world.players().forEach(player -> {
            ChunkPos playerChunkPos = ChunkPos.containing(player.blockPosition());

            for (int i = 0; i < CHUNKS_PER_TICK; i++) {
                int offsetX = RANDOM.nextInt(9) - 4;
                int offsetZ = RANDOM.nextInt(9) - 4;

                ChunkPos targetChunk = new ChunkPos(
                        playerChunkPos.x() + offsetX,
                        playerChunkPos.z() + offsetZ
                );

                LevelChunk chunk = world.getChunk(targetChunk.x(), targetChunk.z());
                if (chunk != null) {
                    processChunk(world, chunk);
                }
            }
        });
    }

    /**
     * Processa um chunk para adicionar vegetação
     */
    private static void processChunk(ServerLevel world, LevelChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.getMiddleBlockX();
        int chunkZ = chunkPos.getMiddleBlockZ();

        chunkSaplingCount.putIfAbsent(chunkPos, 0);

        for (int i = 0; i < BLOCKS_PER_CHUNK; i++) {
            int x = chunkX + RANDOM.nextInt(16);
            int z = chunkZ + RANDOM.nextInt(16);

            BlockPos topPos = new BlockPos(
                    x,
                    world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z),
                    z
            );

            if (RANDOM.nextDouble() < SPAWN_CHANCE) {
                boolean saplingSpawnEnabled = AdventureSeasonConfig.isSpringSaplingSpawnEnabled();
                int currentSaplings = chunkSaplingCount.get(chunkPos);
                boolean canSpawnSapling = saplingSpawnEnabled
                        && currentSaplings < AdventureSeasonConfig.getMaxSaplingsPerChunk();

                double spawnRoll = RANDOM.nextDouble();

                if (canSpawnSapling && spawnRoll < AdventureSeasonConfig.getSpringSaplingSpawnChance()) {
                    if (trySpawnSapling(world, topPos)) {
                        chunkSaplingCount.put(chunkPos, currentSaplings + 1);
                    }
                } else {
                    trySpawnVegetation(world, topPos);
                }
            }
        }
    }

    /**
     * Tenta spawnar uma sapling em uma posição
     * Retorna true se conseguiu spawnar
     */
    private static boolean trySpawnSapling(ServerLevel world, BlockPos pos) {
        Holder<Biome> biomeEntry = world.getBiome(pos);
        Biome biome = biomeEntry.value();
        Identifier biomeId = BiomeCache.get(biome);

        if (biomeId != null && AdventureSeasonConfig.isExcludedBiome(biomeId)) {
            return false;
        }

        if (!isValidSpawnLocation(world, pos)) {
            return false;
        }

        List<Block> saplings = getSaplingsForBiome(biomeId);
        if (saplings.isEmpty()) {
            return false;
        }

        Block saplingToSpawn = saplings.get(RANDOM.nextInt(saplings.size()));

        world.setBlockAndUpdate(pos, saplingToSpawn.defaultBlockState());
        return true;
    }

    /**
     * Verifica se o bloco é uma flor
     */
    private static boolean isFlower(Block block) {
        return block == Blocks.DANDELION ||
                block == Blocks.POPPY ||
                block == Blocks.BLUE_ORCHID ||
                block == Blocks.ALLIUM ||
                block == Blocks.AZURE_BLUET ||
                block == Blocks.RED_TULIP ||
                block == Blocks.ORANGE_TULIP ||
                block == Blocks.WHITE_TULIP ||
                block == Blocks.PINK_TULIP ||
                block == Blocks.OXEYE_DAISY ||
                block == Blocks.CORNFLOWER ||
                block == Blocks.LILY_OF_THE_VALLEY ||
                block == Blocks.WITHER_ROSE ||
                block == Blocks.SUNFLOWER ||
                block == Blocks.LILAC ||
                block == Blocks.ROSE_BUSH ||
                block == Blocks.PEONY;
    }

    /**
     * Tenta spawnar vegetação em uma posição
     * Retorna true se conseguiu spawnar
     */
    private static boolean trySpawnVegetation(ServerLevel world, BlockPos pos) {
        Holder<Biome> biomeEntry = world.getBiome(pos);
        Biome biome = biomeEntry.value();
        Identifier biomeId = BiomeCache.get(biome);

        if (biomeId != null && AdventureSeasonConfig.isExcludedBiome(biomeId)) {
            return false;
        }

        if (!isValidSpawnLocation(world, pos)) {
            return false;
        }

        List<Block> vegetation = getVegetationForBiome(biomeId);
        if (vegetation.isEmpty()) {
            return false;
        }

        Block blockToSpawn = vegetation.get(RANDOM.nextInt(vegetation.size()));

        if (isFlower(blockToSpawn)) {
            if (RANDOM.nextDouble() >= FLOWER_CHANCE) {
                blockToSpawn = RANDOM.nextBoolean() ? Blocks.SHORT_GRASS : Blocks.TALL_GRASS;
            }
        }

        spawnVegetation(world, pos, blockToSpawn);
        return true;
    }

    /**
     * Verifica se a posição é válida para spawnar vegetação
     */
    private static boolean isValidSpawnLocation(ServerLevel world, BlockPos pos) {
        BlockState groundState = world.getBlockState(pos.below());
        BlockState currentState = world.getBlockState(pos);

        if (!groundState.is(BlockTags.DIRT) &&
                groundState.getBlock() != Blocks.GRASS_BLOCK &&
                groundState.getBlock() != Blocks.PODZOL &&
                groundState.getBlock() != Blocks.MYCELIUM) {
            return false;
        }

        if (!currentState.isAir()) {
            return false;
        }

        if (world.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos) < 8) {
            return false;
        }

        return true;
    }

    /**
     * Retorna a lista de saplings apropriadas para o bioma
     */
    private static List<Block> getSaplingsForBiome(Identifier biomeId) {
        if (biomeId == null) {
            return BIOME_SAPLINGS.get("default");
        }

        String biomePath = biomeId.getPath();

        for (Map.Entry<String, List<Block>> entry : BIOME_SAPLINGS.entrySet()) {
            if (biomePath.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return BIOME_SAPLINGS.get("default");
    }

    /**
     * Retorna a lista de vegetação apropriada para o bioma
     */
    private static List<Block> getVegetationForBiome(Identifier biomeId) {
        if (biomeId == null) {
            return BIOME_VEGETATION.get("default");
        }

        String biomePath = biomeId.getPath();

        for (Map.Entry<String, List<Block>> entry : BIOME_VEGETATION.entrySet()) {
            if (biomePath.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return BIOME_VEGETATION.get("default");
    }

    /**
     * Spawna a vegetação na posição
     */
    private static void spawnVegetation(ServerLevel world, BlockPos pos, Block block) {
        if (block == Blocks.TALL_GRASS ||
                block == Blocks.LARGE_FERN ||
                block == Blocks.SUNFLOWER ||
                block == Blocks.LILAC ||
                block == Blocks.ROSE_BUSH ||
                block == Blocks.PEONY) {

            if (!world.getBlockState(pos.above()).isAir()) {
                return;
            }

            BlockState lowerState = block.defaultBlockState()
                    .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
            world.setBlockAndUpdate(pos, lowerState);

            BlockState upperState = block.defaultBlockState()
                    .setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
            world.setBlockAndUpdate(pos.above(), upperState);

        } else {
            world.setBlockAndUpdate(pos, block.defaultBlockState());
        }
    }

    /**
     * Força o crescimento de vegetação em uma área (útil para comandos)
     */
    public static int forceSpawnInArea(ServerLevel world, BlockPos center, int radius, int amount) {
        int spawned = 0;
        int attempts = 0;
        int maxAttempts = amount * 10;

        while (spawned < amount && attempts < maxAttempts) {
            attempts++;

            int x = center.getX() + RANDOM.nextInt(radius * 2 + 1) - radius;
            int z = center.getZ() + RANDOM.nextInt(radius * 2 + 1) - radius;

            BlockPos topPos = new BlockPos(
                    x,
                    world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z),
                    z
            );

            if (isValidSpawnLocation(world, topPos)) {
                Holder<Biome> biomeEntry = world.getBiome(topPos);
                Identifier biomeId = BiomeCache.get(biomeEntry.value());

                if (biomeId == null || !AdventureSeasonConfig.isExcludedBiome(biomeId)) {
                    List<Block> vegetation = getVegetationForBiome(biomeId);
                    if (!vegetation.isEmpty()) {
                        Block block = vegetation.get(RANDOM.nextInt(vegetation.size()));
                        spawnVegetation(world, topPos, block);
                        spawned++;
                    }
                }
            }
        }

        return spawned;
    }
}