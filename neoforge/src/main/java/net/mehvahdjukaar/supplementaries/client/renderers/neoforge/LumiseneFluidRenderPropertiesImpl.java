package net.mehvahdjukaar.supplementaries.client.renderers.neoforge;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.client.LumiseneFluidRenderProperties;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Vector3f;

public class LumiseneFluidRenderPropertiesImpl extends LumiseneFluidRenderProperties implements IClientFluidTypeExtensions {

    @Override
    public ResourceLocation getFlowingTexture() {
        return SINGLE_FLOWING_TEXTURE;
    }

    @Override
    public ResourceLocation getStillTexture() {
        return SINGLE_STILL_TEXTURE;
    }

    @Override
    public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
        return UNDERWATER_TEXTURE;
    }

    @Override
    public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
        return new Vector3f(1F, 246/255f, 208/255f);
    }

    @Override
    public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
        RenderSystem.setShaderFogStart(0.1f);
        RenderSystem.setShaderFogEnd(8);
    }
}
