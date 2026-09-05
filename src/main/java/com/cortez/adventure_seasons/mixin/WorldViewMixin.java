package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.AdventureSeason;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelReader.class)
public interface WorldViewMixin {

    @Shadow
    BiomeManager getBiomeManager();

    @Overwrite
    default Holder<Biome> getBiome(BlockPos pos) {
        Holder<Biome> biomeEntry = this.getBiomeManager().getBiome(pos);
        if (this instanceof Level) {
            AdventureSeason.injectBiomeTemperature(biomeEntry, (Level) this);
        }
        return biomeEntry;
    }
}
