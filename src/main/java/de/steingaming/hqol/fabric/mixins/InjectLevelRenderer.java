package de.steingaming.hqol.fabric.mixins;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import de.steingaming.hqol.fabric.helper.RenderHelper;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class InjectLevelRenderer {

    //? if >= 26.1 {
    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void beginRender(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, net.minecraft.client.renderer.state.level.CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci) {
        RenderHelper.beginRender(deltaTracker);
    }
    //?} else {
    
    /*@Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void beginRender(GraphicsResourceAllocator allocator, DeltaTracker tickCounter, boolean renderBlockOutline, net.minecraft.client.Camera camera, org.joml.Matrix4f positionMatrix, org.joml.Matrix4f matrix4f, org.joml.Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, org.joml.Matrix4f fogColor, boolean renderSky, CallbackInfo ci) {
        RenderHelper.beginRender(tickCounter);
    }
     
    *///?}
}
