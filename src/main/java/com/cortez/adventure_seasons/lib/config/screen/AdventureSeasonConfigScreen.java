package com.cortez.adventure_seasons.lib.config.screen;

import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class AdventureSeasonConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Adventure Seasons"));

        builder.setSavingRunnable(() -> {
            // aqui você pode salvar outras configs se quiser
        });

        ConfigCategory general = builder.getOrCreateCategory(
                Text.literal("Geral")
        );

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(
                entryBuilder.startBooleanToggle(
                                Text.translatable("adventure_seasons.config_label"),
                                AdventureSeasonConfig.isServer()
                        )
                        .setDefaultValue(false)
                        .setTooltip(
                                Text.translatable("adventure_seasons.config_text")
                        )
                        .setSaveConsumer(AdventureSeasonConfig::setServer)
                        .build()
        );

        return builder.build();
    }
}
