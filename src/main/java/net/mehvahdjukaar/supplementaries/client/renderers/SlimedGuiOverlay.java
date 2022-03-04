package net.mehvahdjukaar.supplementaries.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;

public class SlimedGuiOverlay extends Gui implements IIngameOverlay {

    public SlimedGuiOverlay(Minecraft pMinecraft) {
        super(pMinecraft);
    }

    public static void register() {
        OverlayRegistry.registerOverlayBelow(ForgeIngameGui.FROSTBITE_ELEMENT, "Slimed", new SlimedGuiOverlay(Minecraft.getInstance()));
    }

    @Override
    public void render(ForgeIngameGui gui, PoseStack mStack, float partialTicks, int width, int height) {
        if (this.minecraft.player != null && false) {
            float alpha = 1;//iHealable.getHealingFade(partialTicks);
            gui.setupOverlayRenderState(true, false);
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            RenderSystem.setShaderTexture(0, Textures.SLIME_GUI_OVERLAY);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.vertex(0.0D, height, -90.0D).uv(0.0F, 1.0F).endVertex();
            bufferbuilder.vertex(width, height, -90.0D).uv(1.0F, 1.0F).endVertex();
            bufferbuilder.vertex(width, 0.0D, -90.0D).uv(1.0F, 0.0F).endVertex();
            bufferbuilder.vertex(0.0D, 0.0D, -90.0D).uv(0.0F, 0.0F).endVertex();
            tesselator.end();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }


}
