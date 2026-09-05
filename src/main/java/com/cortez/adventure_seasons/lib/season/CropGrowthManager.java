package com.cortez.adventure_seasons.lib.season;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gerencia a velocidade de crescimento das plantações baseado nas subestações.
 * MID_SPRING é a melhor época para crescimento.
 */
public class CropGrowthManager {

    /**
     * Retorna o modificador de crescimento para a subestação atual.
     * Valores maiores que 1.0 = crescimento mais rápido
     * Valores menores que 1.0 = crescimento mais lento
     *
     * @param subSeason A subestação atual
     * @return O modificador de crescimento
     */
    public static float getGrowthModifier(Season.SubSeason subSeason) {
        boolean isReversed = AdventureSeasonConfig.isFallAndSpringReversed();

        return switch (subSeason) {
            // PRIMAVERA - Época de crescimento
            case EARLY_SPRING -> isReversed ? 0.6f : 1.3f;
            case MID_SPRING -> isReversed ? 0.5f : 2.0f;
            case LATE_SPRING -> isReversed ? 0.7f : 1.5f;

            // VERÃO - Crescimento moderado (pode ser muito quente)
            case EARLY_SUMMER -> 1.2f;
            case MID_SUMMER -> 0.9f;
            case LATE_SUMMER -> 1.0f;

            // OUTONO - Crescimento reduzido
            case EARLY_AUTUMN -> isReversed ? 1.3f : 0.8f;
            case MID_AUTUMN -> isReversed ? 2.0f : 0.6f;
            case LATE_AUTUMN -> isReversed ? 1.5f : 0.5f;

            // INVERNO - Crescimento muito lento
            case EARLY_WINTER -> 0.3f;
            case MID_WINTER -> 0.1f;
            case LATE_WINTER -> 0.4f;
        };
    }

    /**
     * Verifica se a plantação deve crescer neste tick, baseado na estação.
     *
     * @param world O mundo do servidor
     * @param pos A posição do bloco
     * @param state O estado do bloco
     * @param random O gerador de números aleatórios
     * @return true se deve permitir o crescimento, false caso contrário
     */
    public static boolean shouldGrow(ServerLevel world, BlockPos pos, BlockState state, RandomSource random) {
        // Verifica se é uma plantação
        if (!(state.getBlock() instanceof CropBlock ||
                state.getBlock() instanceof StemBlock ||
                state.getBlock() instanceof SaplingBlock ||
                state.getBlock() instanceof SweetBerryBushBlock ||
                state.getBlock() instanceof NetherWartBlock ||
                state.getBlock() instanceof BambooStalkBlock ||
                state.getBlock() instanceof SugarCaneBlock ||
                state.getBlock() instanceof CactusBlock)) {
            return true; // Não é plantação, permite crescimento normal
        }

        // Verifica se o bioma está excluído
        var biomeEntry = world.getBiome(pos);
        var biomeId = biomeEntry.unwrapKey()
                .map(ResourceKey::identifier)
                .orElse(null);

        if (biomeId != null && AdventureSeasonConfig.isExcludedBiome(biomeId)) {
            return true; // Bioma excluído, crescimento normal
        }

        // Obtém a subestação atual
        Season.SubSeason subSeason = SeasonState.getSubSeason();
        if (subSeason == null) {
            return true; // Sem informação de estação, permite crescimento normal
        }

        // Calcula a chance de crescimento baseada no modificador
        float growthModifier = getGrowthModifier(subSeason);

        float growthChance = growthModifier;

        // Se o modificador é maior que 1, pode crescer múltiplas vezes
        if (growthModifier > 1.0f) {
            int guaranteedGrowths = (int) growthModifier;
            float remainingChance = growthModifier - guaranteedGrowths;

            for (int i = 1; i < guaranteedGrowths; i++) {
                tryGrowPlant(world, pos, state);
            }

            return random.nextFloat() < remainingChance;
        } else {
            return random.nextFloat() < growthChance;
        }
    }

    /**
     * Tenta fazer a planta crescer um estágio.
     */
    private static void tryGrowPlant(ServerLevel world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        if (block instanceof CropBlock crop) {
            if (!crop.isMaxAge(state)) {
                world.setBlock(pos, crop.getStateForAge(crop.getAge(state) + 1), Block.UPDATE_NEIGHBORS);
            }
        } else if (block instanceof StemBlock stem) {
            int age = state.getValue(StemBlock.AGE);
            if (age < 7) {
                world.setBlock(pos, state.setValue(StemBlock.AGE, age + 1), Block.UPDATE_NEIGHBORS);
            }
        } else if (block instanceof SaplingBlock sapling) {
            sapling.advanceTree(world, pos, state, world.getRandom());
        } else if (block instanceof SweetBerryBushBlock berry) {
            int age = state.getValue(SweetBerryBushBlock.AGE);
            if (age < 3) {
                world.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, age + 1), Block.UPDATE_NEIGHBORS);
            }
        } else if (block instanceof NetherWartBlock wart) {
            int age = state.getValue(NetherWartBlock.AGE);
            if (age < 3) {
                world.setBlock(pos, state.setValue(NetherWartBlock.AGE, age + 1), Block.UPDATE_NEIGHBORS);
            }
        } else if (block instanceof SugarCaneBlock) {
            growStackableAgeBlock(world, pos, state, block, SugarCaneBlock.AGE);
        } else if (block instanceof CactusBlock) {
            growStackableAgeBlock(world, pos, state, block, CactusBlock.AGE);
        }
    }

    /**
     * Replica a lógica vanilla de crescimento de blocos empilháveis
     * (cana-de-açúcar e cacto), que usam a propriedade AGE (0-15) e
     * empilham um novo segmento acima ao atingir a idade máxima,
     * respeitando o limite de altura de 3 blocos.
     */
    private static void growStackableAgeBlock(ServerLevel world, BlockPos pos, BlockState state, Block block,
                                              net.minecraft.world.level.block.state.properties.IntegerProperty ageProperty) {
        BlockPos abovePos = pos.above();
        if (!world.isEmptyBlock(abovePos)) {
            return;
        }

        int height = 1;
        while (world.getBlockState(pos.below(height)).is(block)) {
            height++;
        }

        if (height >= 3) {
            return;
        }

        int age = state.getValue(ageProperty);
        if (age == 15) {
            world.setBlockAndUpdate(abovePos, block.defaultBlockState());
            world.setBlock(pos, state.setValue(ageProperty, 0), Block.UPDATE_CLIENTS);
        } else {
            world.setBlock(pos, state.setValue(ageProperty, age + 1), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Retorna uma mensagem descritiva sobre a velocidade de crescimento.
     */
    public static String getGrowthDescription(Season.SubSeason subSeason) {
        float modifier = getGrowthModifier(subSeason);

        if (modifier >= 1.8f) {
            return "§a§l✦ Crescimento Excelente!";
        } else if (modifier >= 1.3f) {
            return "§a✦ Crescimento Rápido";
        } else if (modifier >= 0.9f) {
            return "§e○ Crescimento Normal";
        } else if (modifier >= 0.5f) {
            return "§6○ Crescimento Lento";
        } else {
            return "§c○ Crescimento Muito Lento";
        }
    }

    /**
     * Loga informações sobre a mudança de estação e crescimento.
     */
    public static void logSeasonChange(Season.SubSeason newSubSeason) {
        float modifier = getGrowthModifier(newSubSeason);
        String description = getGrowthDescription(newSubSeason);

        AdventureSeasons.LOGGER.info(String.format(
                "[Adventure Seasons] Modificador de crescimento: %.1fx - %s",
                modifier,
                description.replaceAll("§[0-9a-flmno]", "")
        ));
    }
}