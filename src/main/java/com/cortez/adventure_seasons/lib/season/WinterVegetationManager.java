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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Random;

public class WinterVegetationManager {

    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;

    // Configurações de remoção de vegetação
    private static final int CHECK_INTERVAL = 5; // Verifica a cada 0.25 segundos
    private static final int CHUNKS_PER_TICK = 8; // Quantos chunks processar por vez
    private static final int BLOCKS_PER_CHUNK = 32; // Quantos blocos verificar por chunk
    private static final int SKY_CHECK_HEIGHT = 5; // Quantos blocos verificar acima para céu aberto

    /**
     * Chamado a cada tick do servidor para remover vegetação no inverno
     */
    public static void tick(ServerWorld world, Season currentSeason) {

        Biome biome = world.getBiome(world.getSpawnPos()).value();
        Identifier id = BiomeCache.get((Biome)(Object)biome);

        if (id != null && AdventureSeasonConfig.isExcludedBiome(id)) {
            return;
        }

        // Só processa se for inverno
        if (currentSeason != Season.WINTER) {
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
     * Processa um chunk para remover vegetação
     */
    private static void processChunk(ServerWorld world, WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.getStartX();
        int chunkZ = chunkPos.getStartZ();

        // Verifica blocos aleatórios no chunk
        for (int i = 0; i < BLOCKS_PER_CHUNK; i++) {
            int x = chunkX + RANDOM.nextInt(16);
            int z = chunkZ + RANDOM.nextInt(16);

            // Pega a altura mais alta com bloco sólido
            BlockPos topPos = world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 0, z));

            // Verifica alguns blocos acima e abaixo
            for (int yOffset = -2; yOffset <= 2; yOffset++) {
                BlockPos checkPos = topPos.add(0, yOffset, 0);

                // Verifica se deve remover vegetação nesta posição
                if (shouldRemoveVegetationAt(world, checkPos)) {
                    removeVegetation(world, checkPos);
                }
            }
        }
    }

    /**
     * Verifica se deve remover vegetação nesta posição
     */
    private static boolean shouldRemoveVegetationAt(ServerWorld world, BlockPos pos) {
        // Verifica o bioma
        RegistryEntry<Biome> biomeEntry = world.getBiome(pos);
        Identifier biomeId = world.getRegistryManager()
                .get(RegistryKeys.BIOME)
                .getId(biomeEntry.value());

        // Não remove em biomas excluídos
        if (biomeId != null && AdventureSeasonConfig.isExcludedBiome(biomeId)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Verifica se é vegetação que pode morrer
        if (!isVegetation(block)) {
            return false;
        }

        // Verifica se está exposto ao céu (ar livre)
        if (!isExposedToSky(world, pos)) {
            return false;
        }

        return true;
    }

    /**
     * Verifica se o bloco está exposto ao céu (não está protegido por teto)
     */
    private static boolean isExposedToSky(ServerWorld world, BlockPos pos) {
        // Verifica se há blocos sólidos acima (teto/proteção)
        for (int y = 1; y <= SKY_CHECK_HEIGHT; y++) {
            BlockPos checkPos = pos.up(y);
            BlockState state = world.getBlockState(checkPos);

            // Se encontrar um bloco sólido/opaco, está protegido
            if (state.isOpaqueFullCube(world, checkPos) ||
                    state.isSolidBlock(world, checkPos)) {
                return false;
            }

            // Verifica blocos específicos que protegem (vidro, folhas, etc)
            Block block = state.getBlock();
            if (isProtectiveBlock(block)) {
                return false;
            }
        }

        // Se não encontrou nenhum bloco protetor, está exposto
        return true;
    }

    /**
     * Verifica se o bloco é considerado protetor (teto/cobertura)
     */
    private static boolean isProtectiveBlock(Block block) {
        // Blocos que protegem plantas do inverno
        return block == Blocks.GLASS ||
                block == Blocks.WHITE_STAINED_GLASS ||
                block == Blocks.ORANGE_STAINED_GLASS ||
                block == Blocks.MAGENTA_STAINED_GLASS ||
                block == Blocks.LIGHT_BLUE_STAINED_GLASS ||
                block == Blocks.YELLOW_STAINED_GLASS ||
                block == Blocks.LIME_STAINED_GLASS ||
                block == Blocks.PINK_STAINED_GLASS ||
                block == Blocks.GRAY_STAINED_GLASS ||
                block == Blocks.LIGHT_GRAY_STAINED_GLASS ||
                block == Blocks.CYAN_STAINED_GLASS ||
                block == Blocks.PURPLE_STAINED_GLASS ||
                block == Blocks.BLUE_STAINED_GLASS ||
                block == Blocks.BROWN_STAINED_GLASS ||
                block == Blocks.GREEN_STAINED_GLASS ||
                block == Blocks.RED_STAINED_GLASS ||
                block == Blocks.BLACK_STAINED_GLASS ||
                block == Blocks.GLASS_PANE ||
                block == Blocks.TINTED_GLASS ||
                block == Blocks.OAK_LEAVES ||
                block == Blocks.SPRUCE_LEAVES ||
                block == Blocks.BIRCH_LEAVES ||
                block == Blocks.JUNGLE_LEAVES ||
                block == Blocks.ACACIA_LEAVES ||
                block == Blocks.DARK_OAK_LEAVES ||
                block == Blocks.MANGROVE_LEAVES ||
                block == Blocks.CHERRY_LEAVES ||
                block == Blocks.AZALEA_LEAVES ||
                block == Blocks.FLOWERING_AZALEA_LEAVES;
    }

    /**
     * Verifica se o bloco é vegetação que deve ser removida
     */
    private static boolean isVegetation(Block block) {
        // Grama e variantes
        if (block == Blocks.SHORT_GRASS ||
                block == Blocks.TALL_GRASS ||
                block == Blocks.FERN ||
                block == Blocks.LARGE_FERN) {
            return true;
        }

        // Flores pequenas
        if (block == Blocks.DANDELION ||
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
                block == Blocks.TORCHFLOWER ||
                block == Blocks.PITCHER_PLANT) {
            return true;
        }

        // Flores grandes
        if (block == Blocks.SUNFLOWER ||
                block == Blocks.LILAC ||
                block == Blocks.ROSE_BUSH ||
                block == Blocks.PEONY) {
            return true;
        }

        // Plantações (Crops)
        if (block == Blocks.WHEAT ||
                block == Blocks.CARROTS ||
                block == Blocks.POTATOES ||
                block == Blocks.BEETROOTS ||
                block == Blocks.TORCHFLOWER_CROP ||
                block == Blocks.PITCHER_CROP) {
            return true;
        }

        // Outras plantas
        if (block == Blocks.SWEET_BERRY_BUSH ||
                block == Blocks.DEAD_BUSH ||
                block == Blocks.NETHER_WART ||
                block == Blocks.COCOA) {
            return true;
        }

        // Plantas aquáticas e do nether que também podem morrer
        if (block == Blocks.SEAGRASS ||
                block == Blocks.TALL_SEAGRASS ||
                block == Blocks.KELP ||
                block == Blocks.KELP_PLANT ||
                block == Blocks.SEA_PICKLE ||
                block == Blocks.CRIMSON_ROOTS ||
                block == Blocks.WARPED_ROOTS) {
            return true;
        }

        return false;
    }

    /**
     * Remove vegetação na posição
     */
    private static void removeVegetation(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Para plantas altas (2 blocos), remove ambas as partes
        if (block == Blocks.TALL_GRASS ||
                block == Blocks.LARGE_FERN ||
                block == Blocks.SUNFLOWER ||
                block == Blocks.LILAC ||
                block == Blocks.ROSE_BUSH ||
                block == Blocks.PEONY ||
                block == Blocks.TALL_SEAGRASS ||
                block == Blocks.PITCHER_PLANT) {

            // Verifica se é a parte de cima ou de baixo
            if (state.contains(TallPlantBlock.HALF)) {
                DoubleBlockHalf half = state.get(TallPlantBlock.HALF);

                if (half == DoubleBlockHalf.LOWER) {
                    // Remove a parte de baixo e a de cima
                    world.removeBlock(pos, false);
                    world.removeBlock(pos.up(), false);
                } else {
                    // Remove a parte de cima e a de baixo
                    world.removeBlock(pos, false);
                    world.removeBlock(pos.down(), false);
                }
            }
        } else {
            // Remove plantas simples
            world.removeBlock(pos, false);
        }
    }

    /**
     * Força a remoção de vegetação em uma área (útil para comandos)
     */
    public static int forceRemoveInArea(ServerWorld world, BlockPos center, int radius) {
        int removed = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -radius; y <= radius; y++) {
                    BlockPos pos = center.add(x, y, z);

                    if (shouldRemoveVegetationAt(world, pos)) {
                        removeVegetation(world, pos);
                        removed++;
                    }
                }
            }
        }

        return removed;
    }
}