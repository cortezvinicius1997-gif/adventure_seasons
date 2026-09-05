package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.AdventureSeason;
import com.cortez.adventure_seasons.lib.config.AdventureSeasonConfig;
import com.cortez.adventure_seasons.lib.util.Meltable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin extends Level implements WorldGenLevel {

    protected ServerWorldMixin(WritableLevelData worldProperties, ResourceKey<Level> dimensionKey, RegistryAccess registryManager, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(worldProperties, dimensionKey, registryManager, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 0), method = "tickPrecipitation(Lnet/minecraft/core/BlockPos;)V", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void setMeltableIce(BlockPos pos, CallbackInfo ci, BlockPos blockPos, BlockPos blockPos2, Biome biome) {
        AdventureSeason.setMeltable(blockPos2);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 1), method = "tickPrecipitation(Lnet/minecraft/core/BlockPos;)V", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void setMeltableLayeredSnow(BlockPos pos, CallbackInfo ci, BlockPos blockPos, BlockPos blockPos2, Biome biome, int i, BlockState blockState, int j, BlockState blockState2) {
        AdventureSeason.setMeltable(blockPos);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 2), method = "tickPrecipitation(Lnet/minecraft/core/BlockPos;)V", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void setMeltableSnow(BlockPos pos, CallbackInfo ci, BlockPos blockPos, BlockPos blockPos2, Biome biome, int i, BlockState blockState) {
        AdventureSeason.setMeltable(blockPos);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;handlePrecipitation(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/biome/Biome$Precipitation;)V"), method = "tickPrecipitation(Lnet/minecraft/core/BlockPos;)V", locals = LocalCapture.CAPTURE_FAILSOFT)
    public void setReplacedMeltable(BlockPos pos, CallbackInfo ci, BlockPos blockPos, BlockPos blockPos2, Biome biome, int i, Biome.Precipitation precipitation, BlockState blockState3) {
        if (AdventureSeasonConfig.shouldSnowReplaceVegetation())
            Meltable.replaceBlockOnSnow((ServerLevel) (Object) this, blockPos, biome);
    }

}
