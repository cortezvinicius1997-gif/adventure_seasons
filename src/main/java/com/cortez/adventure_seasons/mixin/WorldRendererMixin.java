package com.cortez.adventure_seasons.mixin;

import com.cortez.adventure_seasons.lib.cache.ColorsCache;
import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class WorldRendererMixin {

    @Inject(at = @At("HEAD"), method = "invalidateCompiledGeometry")
    public void reload(ClientLevel level, Options options, Camera camera, BlockColors blockColors, CallbackInfo ci) {
        ColorsCache.clearCache();
    }
}
