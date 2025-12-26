package com.cortez.adventure_seasons.lib.season;

import com.cortez.adventure_seasons.AdventureSeasons;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

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
            case EARLY_SPRING -> isReversed ? 0.6f : 1.3f;   // Começando a melhorar
            case MID_SPRING -> isReversed ? 0.5f : 2.0f;     // MELHOR ÉPOCA! 🌱
            case LATE_SPRING -> isReversed ? 0.7f : 1.5f;    // Ainda boa

            // VERÃO - Crescimento moderado (pode ser muito quente)
            case EARLY_SUMMER -> 1.2f;
            case MID_SUMMER -> 0.9f;    // Muito quente, desacelera
            case LATE_SUMMER -> 1.0f;

            // OUTONO - Crescimento reduzido
            case EARLY_AUTUMN -> isReversed ? 1.3f : 0.8f;
            case MID_AUTUMN -> isReversed ? 2.0f : 0.6f;     // Se invertido, é primavera
            case LATE_AUTUMN -> isReversed ? 1.5f : 0.5f;

            // INVERNO - Crescimento muito lento
            case EARLY_WINTER -> 0.3f;
            case MID_WINTER -> 0.1f;    // Quase parado
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
    public static boolean shouldGrow(ServerWorld world, BlockPos pos, BlockState state, Random random) {
        // Verifica se é uma plantação
        if (!(state.getBlock() instanceof CropBlock ||
                state.getBlock() instanceof StemBlock ||
                state.getBlock() instanceof SaplingBlock ||
                state.getBlock() instanceof SweetBerryBushBlock ||
                state.getBlock() instanceof NetherWartBlock ||
                state.getBlock() instanceof BambooBlock)) {
            return true; // Não é plantação, permite crescimento normal
        }

        // Verifica se o bioma está excluído
        var biomeEntry = world.getBiome(pos);
        Identifier biomeId = biomeEntry.getKey()
                .map(key -> key.getValue())
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

        // Quanto maior o modificador, maior a chance de crescer
        // Modificador 2.0 = 100% de chance extra (sempre cresce se passar nas outras verificações)
        // Modificador 0.5 = 50% de chance (cresce metade das vezes)
        // Modificador 0.1 = 10% de chance (muito raro)

        float growthChance = growthModifier;

        // Se o modificador é maior que 1, pode crescer múltiplas vezes
        if (growthModifier > 1.0f) {
            // Para cada 1.0 além do primeiro, garante crescimento
            int guaranteedGrowths = (int) growthModifier;
            float remainingChance = growthModifier - guaranteedGrowths;

            // Aplica os crescimentos garantidos
            for (int i = 1; i < guaranteedGrowths; i++) {
                tryGrowPlant(world, pos, state);
            }

            // Verifica a chance restante
            return random.nextFloat() < remainingChance;
        } else {
            // Para modificadores menores que 1, é uma chance simples
            return random.nextFloat() < growthChance;
        }
    }

    /**
     * Tenta fazer a planta crescer um estágio.
     */
    private static void tryGrowPlant(ServerWorld world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();

        if (block instanceof CropBlock crop) {
            if (!crop.isMature(state)) {
                world.setBlockState(pos, crop.withAge(crop.getAge(state) + 1), Block.NOTIFY_LISTENERS);
            }
        } else if (block instanceof StemBlock stem) {
            int age = state.get(StemBlock.AGE);
            if (age < 7) {
                world.setBlockState(pos, state.with(StemBlock.AGE, age + 1), Block.NOTIFY_LISTENERS);
            }
        } else if (block instanceof SaplingBlock sapling) {
            sapling.generate(world, pos, state, world.getRandom());
        } else if (block instanceof SweetBerryBushBlock berry) {
            int age = state.get(SweetBerryBushBlock.AGE);
            if (age < 3) {
                world.setBlockState(pos, state.with(SweetBerryBushBlock.AGE, age + 1), Block.NOTIFY_LISTENERS);
            }
        } else if (block instanceof NetherWartBlock wart) {
            int age = state.get(NetherWartBlock.AGE);
            if (age < 3) {
                world.setBlockState(pos, state.with(NetherWartBlock.AGE, age + 1), Block.NOTIFY_LISTENERS);
            }
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
                description.replaceAll("§[0-9a-flmno]", "") // Remove códigos de cor para o log
        ));
    }
}