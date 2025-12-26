package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.AdventureSeason;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldView.class)
public interface WorldViewMixin {

    @Shadow
    BiomeAccess getBiomeAccess();

    @Overwrite
    default RegistryEntry<Biome> getBiome(BlockPos pos) {
        RegistryEntry<Biome> biomeEntry = this.getBiomeAccess().getBiome(pos);
        if (this instanceof World) {
            AdventureSeason.injectBiomeTemperature(biomeEntry, (World) this);
        }
        return biomeEntry;
    }
}
