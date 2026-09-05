package com.cortez.adventure_seasons.lib.config.screen;

import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import com.cortez.adventure_seasons.lib.season.Season;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class AdventureSeasonConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Adventure Seasons"));

        builder.setSavingRunnable(() -> {
        });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // ==================== GERAL ====================
        ConfigCategory general = builder.getOrCreateCategory(
                Component.literal("Geral")
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.translatable("adventure_seasons.config_label"),
                                AdventureSeasonConfig.isServer()
                        )
                        .setDefaultValue(false)
                        .setTooltip(
                                Component.translatable("adventure_seasons.config_text")
                        )
                        .setSaveConsumer(AdventureSeasonConfig::setServer)
                        .build()
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.literal("Modo Debug"),
                                AdventureSeasonConfig.isDebug()
                        )
                        .setDefaultValue(false)
                        .setTooltip(Component.literal("Mostra informações extras na tela (estação, bioma, temperatura)."))
                        .setSaveConsumer(AdventureSeasonConfig::setDebug)
                        .build()
        );

        general.addEntry(
                entryBuilder.startEnumSelector(
                                Component.literal("Estação Inicial"),
                                Season.class,
                                AdventureSeasonConfig.getStartingSeason()
                        )
                        .setDefaultValue(Season.SPRING)
                        .setTooltip(Component.literal("Estação com a qual um mundo NOVO vai começar."))
                        .setSaveConsumer(AdventureSeasonConfig::setSeasonStart)
                        .build()
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.literal("Chuva Constante no Inverno"),
                                AdventureSeasonConfig.isWinterRain()
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("Força chuva/neve constante durante o meio do inverno."))
                        .setSaveConsumer(AdventureSeasonConfig::setWinterRain)
                        .build()
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.literal("Alterar Temperatura dos Biomas"),
                                AdventureSeasonConfig.isDoTemperatureChange()
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("Se desativado, as estações não alteram a temperatura/neve dos biomas."))
                        .setSaveConsumer(AdventureSeasonConfig::setDoTemperatureChange)
                        .build()
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.literal("Inverter Primavera/Outono"),
                                AdventureSeasonConfig.isFallAndSpringReversed()
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("Inverte os modificadores de temperatura e crescimento entre primavera e outono."))
                        .setSaveConsumer(AdventureSeasonConfig::setFallAndSpringReversed)
                        .build()
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.literal("Neve Derrete no Verão"),
                                AdventureSeasonConfig.shouldSnowyBiomesMeltInSummer()
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("Se biomas naturalmente nevados devem derreter durante o verão."))
                        .setSaveConsumer(AdventureSeasonConfig::setShouldSnowyBiomesMeltInSummer)
                        .build()
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.literal("Gelo Perto da Água Derrete"),
                                AdventureSeasonConfig.isShouldIceNearWaterMelt()
                        )
                        .setDefaultValue(false)
                        .setTooltip(Component.literal("Se o gelo perto de fontes de água deve derreter fora do inverno."))
                        .setSaveConsumer(AdventureSeasonConfig::setShouldIceNearWaterMelt)
                        .build()
        );

        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.literal("Neve Substitui Vegetação"),
                                AdventureSeasonConfig.shouldSnowReplaceVegetation()
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("Se a neve deve cobrir/substituir grama, flores etc. no inverno."))
                        .setSaveConsumer(AdventureSeasonConfig::setShouldSnowReplaceVegetation)
                        .build()
        );

        // ==================== MUDAS (SAPLINGS) ====================
        ConfigCategory saplings = builder.getOrCreateCategory(
                Component.literal("Mudas na Primavera")
        );

        saplings.addEntry(
                entryBuilder.startBooleanToggle(
                                Component.literal("Nascimento Espontâneo de Mudas"),
                                AdventureSeasonConfig.isSpringSaplingSpawnEnabled()
                        )
                        .setDefaultValue(true)
                        .setTooltip(Component.literal("Se mudas podem nascer sozinhas (no lugar de grama/flores) durante a primavera."))
                        .setSaveConsumer(AdventureSeasonConfig::setSpringSaplingSpawnEnabled)
                        .build()
        );

        saplings.addEntry(
                entryBuilder.startDoubleField(
                                Component.literal("Chance de Nascimento (0.0 a 1.0)"),
                                AdventureSeasonConfig.getSpringSaplingSpawnChance()
                        )
                        .setDefaultValue(0.003)
                        .setMin(0.0)
                        .setMax(1.0)
                        .setTooltip(Component.literal("Chance de cada posição elegível virar uma muda. Padrão: 0.003 (0.3%)."))
                        .setSaveConsumer(AdventureSeasonConfig::setSpringSaplingSpawnChance)
                        .build()
        );

        saplings.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Máximo de Mudas por Chunk"),
                                AdventureSeasonConfig.getMaxSaplingsPerChunk()
                        )
                        .setDefaultValue(1)
                        .setMin(0)
                        .setTooltip(Component.literal("Máximo de mudas que podem nascer sozinhas por chunk, durante uma mesma primavera."))
                        .setSaveConsumer(AdventureSeasonConfig::setMaxSaplingsPerChunk)
                        .build()
        );

        // ==================== DURAÇÃO DAS ESTAÇÕES ====================
        ConfigCategory lengths = builder.getOrCreateCategory(
                Component.literal("Duração das Estações")
        );

        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Primavera - Início (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getSpring().getEarlyLength()
                        )
                        .setMin(0)
                        .setTooltip(Component.literal("24000 ticks = 1 dia in-game."))
                        .setSaveConsumer(AdventureSeasonConfig::setSpringEarlyLength)
                        .build()
        );
        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Primavera - Meio (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getSpring().getMidLength()
                        )
                        .setMin(0)
                        .setSaveConsumer(AdventureSeasonConfig::setSpringMidLength)
                        .build()
        );
        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Primavera - Fim (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getSpring().getLateLength()
                        )
                        .setMin(0)
                        .setSaveConsumer(AdventureSeasonConfig::setSpringLateLength)
                        .build()
        );

        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Verão - Início (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getSummer().getEarlyLength()
                        )
                        .setMin(0)
                        .setSaveConsumer(AdventureSeasonConfig::setSummerEarlyLength)
                        .build()
        );
        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Verão - Meio (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getSummer().getMidLength()
                        )
                        .setMin(0)
                        .setSaveConsumer(AdventureSeasonConfig::setSummerMidLength)
                        .build()
        );
        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Verão - Fim (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getSummer().getLateLength()
                        )
                        .setMin(0)
                        .setSaveConsumer(AdventureSeasonConfig::setSummerLateLength)
                        .build()
        );

        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Outono - Início (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getAutumn().getEarlyLength()
                        )
                        .setMin(0)
                        .setSaveConsumer(AdventureSeasonConfig::setAutumnEarlyLength)
                        .build()
        );
        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Outono - Meio (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getAutumn().getMidLength()
                        )
                        .setMin(0)
                        .setSaveConsumer(AdventureSeasonConfig::setAutumnMidLength)
                        .build()
        );
        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Outono - Fim (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getAutumn().getLateLength()
                        )
                        .setMin(0)
                        .setSaveConsumer(AdventureSeasonConfig::setAutumnLateLength)
                        .build()
        );

        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Inverno - Início (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getWinter().getEarlyLength()
                        )
                        .setMin(0)
                        .setSaveConsumer(AdventureSeasonConfig::setWinterEarlyLength)
                        .build()
        );
        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Inverno - Meio (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getWinter().getMidLength()
                        )
                        .setMin(0)
                        .setSaveConsumer(AdventureSeasonConfig::setWinterMidLength)
                        .build()
        );
        lengths.addEntry(
                entryBuilder.startIntField(
                                Component.literal("Inverno - Fim (ticks)"),
                                AdventureSeasonConfig.getTicksPerSeason().getWinter().getLateLength()
                        )
                        .setMin(0)
                        .setSaveConsumer(AdventureSeasonConfig::setWinterLateLength)
                        .build()
        );

        // ==================== BIOMAS ====================
        ConfigCategory biomes = builder.getOrCreateCategory(
                Component.literal("Biomas")
        );

        biomes.addEntry(
                entryBuilder.startStrList(
                                Component.literal("Biomas Excluídos"),
                                new ArrayList<>(AdventureSeasonConfig.getExcludedBiomes())
                        )
                        .setTooltip(Component.literal("Biomas que não sofrem alteração de temperatura/estação (ex: minecraft:desert)."))
                        .setSaveConsumer(AdventureSeasonConfig::setExcludedBiomes)
                        .build()
        );

        biomes.addEntry(
                entryBuilder.startStrList(
                                Component.literal("Biomas com Neve Forçada no Inverno"),
                                new ArrayList<>(AdventureSeasonConfig.getBiomeForceSnowInWinterList())
                        )
                        .setTooltip(Component.literal("Biomas que sempre recebem neve no inverno, mesmo que normalmente não nevem (ex: minecraft:plains)."))
                        .setSaveConsumer(AdventureSeasonConfig::setBiomeForceSnowInWinterList)
                        .build()
        );

        return builder.build();
    }
}
