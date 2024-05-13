package net.mehvahdjukaar.supplementaries.client.renderers.forge;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.client.LumiseneFluidRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Vector3f;

public class LumiseneFluidRendererImpl extends LumiseneFluidRenderer implements IClientFluidTypeExtensions {

    @Override
    public ResourceLocation getFlowingTexture() {
        return SINGLE_TEXTURE;
    }

    @Override
    public ResourceLocation getStillTexture() {
        return SINGLE_TEXTURE;
    }

    @Override
    public ResourceLocation getRenderOverlayTexture(Minecraft mc) {
        return UNDERWATER_TEXTURE;
    }

    @Override
    public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
        return new Vector3f(1F, 0.8F, 0.01F);
    }

    @Override
    public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
        RenderSystem.setShaderFogStart(0.1f);
        RenderSystem.setShaderFogEnd(8);
    }
}
