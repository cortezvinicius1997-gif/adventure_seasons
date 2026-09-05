package com.cortez.adventure_seasons.client;

import com.cortez.adventure_seasons.datagen.AdventureSeasonsBlockTagsProvider;
import com.cortez.adventure_seasons.datagen.AdventureSeasonsRecipeProvider;
import com.cortez.adventure_seasons.datagen.AdventureSeasonsTableProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class AdventureSeasonsDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(AdventureSeasonsRecipeProvider::new);
        pack.addProvider(AdventureSeasonsTableProvider::new);
        pack.addProvider(AdventureSeasonsBlockTagsProvider::new);
    }
}
