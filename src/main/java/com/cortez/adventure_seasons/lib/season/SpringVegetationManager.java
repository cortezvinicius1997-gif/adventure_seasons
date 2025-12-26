package com.cortez.adventure_seasons.lib.season;

import com.cortez.adventure_seasons.lib.cache.BiomeCache;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;

public class SpringVegetationManager {

    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;

    // Configurações de crescimento de vegetação
    private static final int CHECK_INTERVAL = 5; // Verifica a cada 0.25 segundos
    private static final int CHUNKS_PER_TICK = 8; // Quantos chunks processar por vez
    private static final int BLOCKS_PER_CHUNK = 16; // Quantos blocos verificar por chunk
    private static final double SPAWN_CHANCE = 0.15; // 15% de chance de spawnar vegetação
    private static final double SAPLING_CHANCE = 0.003; // 0.003% de chance de spawnar uma sapling
    private static final double FLOWER_CHANCE = 0.03; // 0.3% de chance de spawnar flores (resto é grama)
    private static final int MAX_SAPLINGS_PER_CHUNK = 1; // Máximo de 3 saplings por chunk

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
    public static void tick(ServerWorld world, Season currentSeason) {
        // Só processa se for primavera
        if (currentSeason != Season.SPRING) {
            // Limpa o contador quando não é primavera
            if (!chunkSaplingCount.isEmpty()) {
                chunkSaplingCount.clear();
            }
            return;
        }

        tickCounter++;

        // Verifica apenas a cada intervalo configurado
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }

        tickCounter = 0;

        // Processa chunks ao redor dos jogadores
        world.getPlayers().forEach(player -> {
            ChunkPos playerChunkPos = new ChunkPos(player.getBlockPos());

            // Processa alguns chunks ao redor do jogador
            for (int i = 0; i < CHUNKS_PER_TICK; i++) {
                int offsetX = RANDOM.nextInt(9) - 4; // -4 a +4
                int offsetZ = RANDOM.nextInt(9) - 4;

                ChunkPos targetChunk = new ChunkPos(
                        playerChunkPos.x + offsetX,
                        playerChunkPos.z + offsetZ
                );

                WorldChunk chunk = world.getChunk(targetChunk.x, targetChunk.z);
                if (chunk != null) {
                    processChunk(world, chunk);
                }
            }
        });
    }

    /**
     * Processa um chunk para adicionar vegetação
     */
    private static void processChunk(ServerWorld world, WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.getStartX();
        int chunkZ = chunkPos.getStartZ();

        // Inicializa o contador de saplings deste chunk se necessário
        chunkSaplingCount.putIfAbsent(chunkPos, 0);

        // Verifica blocos aleatórios no chunk
        for (int i = 0; i < BLOCKS_PER_CHUNK; i++) {
            int x = chunkX + RANDOM.nextInt(16);
            int z = chunkZ + RANDOM.nextInt(16);

            // Pega a altura mais alta com bloco sólido
            BlockPos topPos = world.getTopPosition(
                    net.minecraft.world.Heightmap.Type.MOTION_BLOCKING,
                    new BlockPos(x, 0, z)
            );

            // Primeiro verifica se passa pela chance geral de spawn (15%)
            if (RANDOM.nextDouble() < SPAWN_CHANCE) {
                // Verifica se pode spawnar sapling (limite de 3 por chunk)
                int currentSaplings = chunkSaplingCount.get(chunkPos);
                boolean canSpawnSapling = currentSaplings < MAX_SAPLINGS_PER_CHUNK;

                // Gera um número aleatório para decidir entre sapling e vegetação
                double spawnRoll = RANDOM.nextDouble();

                // Decide se vai spawnar sapling (apenas se pode e passar na chance)
                if (canSpawnSapling && spawnRoll < SAPLING_CHANCE) {
                    if (trySpawnSapling(world, topPos)) {
                        // Incrementa o contador se conseguiu spawnar
                        chunkSaplingCount.put(chunkPos, currentSaplings + 1);
                    }
                } else {
                    // Spawna vegetação normal (grama, flores, etc)
                    trySpawnVegetation(world, topPos);
                }
            }
        }
    }

    /**
     * Tenta spawnar uma sapling em uma posição
     * Retorna true se conseguiu spawnar
     */
    private static boolean trySpawnSapling(ServerWorld world, BlockPos pos) {
        // Verifica o bioma
        RegistryEntry<Biome> biomeEntry = world.getBiome(pos);
        Biome biome = biomeEntry.value();
        Identifier biomeId = BiomeCache.get(biome);

        // Não spawna em biomas excluídos
        if (biomeId != null && AdventureSeasonConfig.isExcludedBiome(biomeId)) {
            return false;
        }

        // Verifica se a posição é válida para spawnar
        if (!isValidSpawnLocation(world, pos)) {
            return false;
        }

        // Pega as saplings apropriadas para o bioma
        List<Block> saplings = getSaplingsForBiome(biomeId);
        if (saplings.isEmpty()) {
            return false;
        }

        // Escolhe uma sapling aleatória
        Block saplingToSpawn = saplings.get(RANDOM.nextInt(saplings.size()));

        // Spawna a sapling
        world.setBlockState(pos, saplingToSpawn.getDefaultState(), 3);
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
    private static boolean trySpawnVegetation(ServerWorld world, BlockPos pos) {
        // Verifica o bioma
        RegistryEntry<Biome> biomeEntry = world.getBiome(pos);
        Biome biome = biomeEntry.value();
        Identifier biomeId = BiomeCache.get(biome);

        // Não spawna em biomas excluídos
        if (biomeId != null && AdventureSeasonConfig.isExcludedBiome(biomeId)) {
            return false;
        }

        // Verifica se a posição é válida para spawnar
        if (!isValidSpawnLocation(world, pos)) {
            return false;
        }

        // Pega a vegetação apropriada para o bioma
        List<Block> vegetation = getVegetationForBiome(biomeId);
        if (vegetation.isEmpty()) {
            return false;
        }

        // Escolhe uma vegetação aleatória
        Block blockToSpawn = vegetation.get(RANDOM.nextInt(vegetation.size()));

        // Se for uma flor, aplica a chance reduzida
        if (isFlower(blockToSpawn)) {
            if (RANDOM.nextDouble() >= FLOWER_CHANCE) {
                // Não spawna a flor, tenta spawnar grama ao invés
                blockToSpawn = RANDOM.nextBoolean() ? Blocks.SHORT_GRASS : Blocks.TALL_GRASS;
            }
        }

        // Spawna a vegetação
        spawnVegetation(world, pos, blockToSpawn);
        return true;
    }

    /**
     * Verifica se a posição é válida para spawnar vegetação
     */
    private static boolean isValidSpawnLocation(ServerWorld world, BlockPos pos) {
        BlockState groundState = world.getBlockState(pos.down());
        BlockState currentState = world.getBlockState(pos);

        // Deve ter um bloco sólido embaixo (grama, terra, etc)
        if (!groundState.isIn(BlockTags.DIRT) &&
                groundState.getBlock() != Blocks.GRASS_BLOCK &&
                groundState.getBlock() != Blocks.PODZOL &&
                groundState.getBlock() != Blocks.MYCELIUM) {
            return false;
        }

        // A posição atual deve estar vazia (ar)
        if (!currentState.isAir()) {
            return false;
        }

        // Verifica se tem luz suficiente
        if (world.getLightLevel(pos) < 8) {
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

        // Tenta encontrar saplings específicas do bioma
        for (Map.Entry<String, List<Block>> entry : BIOME_SAPLINGS.entrySet()) {
            if (biomePath.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Retorna sapling padrão se não encontrar
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

        // Tenta encontrar vegetação específica do bioma
        for (Map.Entry<String, List<Block>> entry : BIOME_VEGETATION.entrySet()) {
            if (biomePath.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Retorna vegetação padrão se não encontrar
        return BIOME_VEGETATION.get("default");
    }

    /**
     * Spawna a vegetação na posição
     */
    private static void spawnVegetation(ServerWorld world, BlockPos pos, Block block) {
        // Verifica se é uma planta alta (2 blocos)
        if (block == Blocks.TALL_GRASS ||
                block == Blocks.LARGE_FERN ||
                block == Blocks.SUNFLOWER ||
                block == Blocks.LILAC ||
                block == Blocks.ROSE_BUSH ||
                block == Blocks.PEONY) {

            // Verifica se tem espaço para 2 blocos
            if (!world.getBlockState(pos.up()).isAir()) {
                return;
            }

            // Coloca a parte de baixo
            BlockState lowerState = block.getDefaultState()
                    .with(TallPlantBlock.HALF, DoubleBlockHalf.LOWER);
            world.setBlockState(pos, lowerState, 3);

            // Coloca a parte de cima
            BlockState upperState = block.getDefaultState()
                    .with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER);
            world.setBlockState(pos.up(), upperState, 3);

        } else {
            // Planta simples (1 bloco)
            world.setBlockState(pos, block.getDefaultState(), 3);
        }
    }

    /**
     * Força o crescimento de vegetação em uma área (útil para comandos)
     */
    public static int forceSpawnInArea(ServerWorld world, BlockPos center, int radius, int amount) {
        int spawned = 0;
        int attempts = 0;
        int maxAttempts = amount * 10; // Evita loop infinito

        while (spawned < amount && attempts < maxAttempts) {
            attempts++;

            // Posição aleatória na área
            int x = center.getX() + RANDOM.nextInt(radius * 2 + 1) - radius;
            int z = center.getZ() + RANDOM.nextInt(radius * 2 + 1) - radius;

            BlockPos topPos = world.getTopPosition(
                    net.minecraft.world.Heightmap.Type.MOTION_BLOCKING,
                    new BlockPos(x, 0, z)
            );

            if (isValidSpawnLocation(world, topPos)) {
                RegistryEntry<Biome> biomeEntry = world.getBiome(topPos);
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