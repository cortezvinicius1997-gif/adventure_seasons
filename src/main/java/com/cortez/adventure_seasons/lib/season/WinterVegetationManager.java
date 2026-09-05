package com.cortez.adventure_seasons.lib.season;

import com.cortez.adventure_seasons.lib.cache.BiomeCache;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
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

public class WinterVegetationManager {

    private static final RandomSource RANDOM = RandomSource.create();
    private static int tickCounter = 0;

    // Configurações de remoção de vegetação
    private static final int CHECK_INTERVAL = 5;
    private static final int CHUNKS_PER_TICK = 8;
    private static final int BLOCKS_PER_CHUNK = 32;
    private static final int SKY_CHECK_HEIGHT = 5;

    /**
     * Chamado a cada tick do servidor para remover vegetação no inverno
     */
    public static void tick(ServerLevel world, Season currentSeason) {

        Holder<Biome> biomeHolder = world.getBiome(world.getRespawnData().pos());
        Biome biome = biomeHolder.value();
        Identifier id = BiomeCache.get(biome);

        if (id != null && AdventureSeasonConfig.isExcludedBiome(id)) {
            return;
        }

        if (currentSeason != Season.WINTER) {
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
     * Processa um chunk para remover vegetação
     */
    private static void processChunk(ServerLevel world, LevelChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int chunkX = chunkPos.getMiddleBlockX();
        int chunkZ = chunkPos.getMiddleBlockZ();

        for (int i = 0; i < BLOCKS_PER_CHUNK; i++) {
            int x = chunkX + RANDOM.nextInt(16);
            int z = chunkZ + RANDOM.nextInt(16);

            BlockPos topPos = new BlockPos(
                    x,
                    world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z),
                    z
            );

            for (int yOffset = -2; yOffset <= 2; yOffset++) {
                BlockPos checkPos = topPos.offset(0, yOffset, 0);

                if (shouldRemoveVegetationAt(world, checkPos)) {
                    removeVegetation(world, checkPos);
                }
            }
        }
    }

    /**
     * Verifica se deve remover vegetação nesta posição
     */
    private static boolean shouldRemoveVegetationAt(ServerLevel world, BlockPos pos) {
        Holder<Biome> biomeEntry = world.getBiome(pos);
        var biomeId = biomeEntry.unwrapKey()
                .map(net.minecraft.resources.ResourceKey::identifier)
                .orElse(null);

        if (biomeId != null && AdventureSeasonConfig.isExcludedBiome(biomeId)) {
            return false;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (!isVegetation(block)) {
            return false;
        }

        if (!isExposedToSky(world, pos)) {
            return false;
        }

        return true;
    }

    /**
     * Verifica se o bloco está exposto ao céu (não está protegido por teto)
     */
    private static boolean isExposedToSky(ServerLevel world, BlockPos pos) {
        for (int y = 1; y <= SKY_CHECK_HEIGHT; y++) {
            BlockPos checkPos = pos.above(y);
            BlockState state = world.getBlockState(checkPos);

            if (state.isSolid() ||
                    state.canOcclude()) {
                return false;
            }

            Block block = state.getBlock();
            if (isProtectiveBlock(block)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Verifica se o bloco é considerado protetor (teto/cobertura)
     */
    private static boolean isProtectiveBlock(Block block) {
        return block == Blocks.GLASS ||
                block == Blocks.STAINED_GLASS.white() ||
                block == Blocks.STAINED_GLASS.orange() ||
                block == Blocks.STAINED_GLASS.magenta() ||
                block == Blocks.STAINED_GLASS.lightBlue() ||
                block == Blocks.STAINED_GLASS.yellow() ||
                block == Blocks.STAINED_GLASS.lime() ||
                block == Blocks.STAINED_GLASS.pink() ||
                block == Blocks.STAINED_GLASS.gray() ||
                block == Blocks.STAINED_GLASS.lightGray() ||
                block == Blocks.STAINED_GLASS.cyan() ||
                block == Blocks.STAINED_GLASS.purple() ||
                block == Blocks.STAINED_GLASS.blue() ||
                block == Blocks.STAINED_GLASS.brown() ||
                block == Blocks.STAINED_GLASS.green() ||
                block == Blocks.STAINED_GLASS.red() ||
                block == Blocks.STAINED_GLASS.black() ||
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
        if (block == Blocks.SHORT_GRASS ||
                block == Blocks.TALL_GRASS ||
                block == Blocks.FERN ||
                block == Blocks.LARGE_FERN) {
            return true;
        }

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

        if (block == Blocks.SUNFLOWER ||
                block == Blocks.LILAC ||
                block == Blocks.ROSE_BUSH ||
                block == Blocks.PEONY) {
            return true;
        }

        if (block == Blocks.WHEAT ||
                block == Blocks.CARROTS ||
                block == Blocks.POTATOES ||
                block == Blocks.BEETROOTS ||
                block == Blocks.TORCHFLOWER_CROP ||
                block == Blocks.PITCHER_CROP) {
            return true;
        }

        if (block == Blocks.SWEET_BERRY_BUSH ||
                block == Blocks.DEAD_BUSH ||
                block == Blocks.NETHER_WART ||
                block == Blocks.COCOA) {
            return true;
        }

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
    private static void removeVegetation(ServerLevel world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.TALL_GRASS ||
                block == Blocks.LARGE_FERN ||
                block == Blocks.SUNFLOWER ||
                block == Blocks.LILAC ||
                block == Blocks.ROSE_BUSH ||
                block == Blocks.PEONY ||
                block == Blocks.TALL_SEAGRASS ||
                block == Blocks.PITCHER_PLANT) {

            if (state.hasProperty(DoublePlantBlock.HALF)) {
                DoubleBlockHalf half = state.getValue(DoublePlantBlock.HALF);

                if (half == DoubleBlockHalf.LOWER) {
                    world.removeBlock(pos, false);
                    world.removeBlock(pos.above(), false);
                } else {
                    world.removeBlock(pos, false);
                    world.removeBlock(pos.below(), false);
                }
            }
        } else {
            world.removeBlock(pos, false);
        }
    }

    /**
     * Força a remoção de vegetação em uma área (útil para comandos)
     */
    public static int forceRemoveInArea(ServerLevel world, BlockPos center, int radius) {
        int removed = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = -radius; y <= radius; y++) {
                    BlockPos pos = center.offset(x, y, z);

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
