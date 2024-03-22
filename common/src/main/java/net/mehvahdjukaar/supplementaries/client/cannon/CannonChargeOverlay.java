package net.mehvahdjukaar.supplementaries.client.cannon;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;

public abstract class CannonChargeOverlay extends Gui {


    private final Minecraft minecraft;
    private final ItemRenderer itemRenderer;

    protected CannonChargeOverlay(Minecraft minecraft, ItemRenderer itemRenderer) {
        super(minecraft, itemRenderer);
        this.itemRenderer = itemRenderer;
        this.minecraft = minecraft;
    }

    public void renderBar(GuiGraphics graphics, float partialTicks, int screenWidth, int screenHeight) {
        if (!minecraft.options.hideGui && CannonController.isActive()) {
            setupOverlayRenderState();
            int i = screenWidth / 2 - 91;

            float f = 0.4f;
            int k = (int) (f * 183.0F);
            int l = screenHeight - 32 + 3;
            graphics.blit(ModTextures.CANNON_ICONS_TEXTURE, i, l, 0, 0, 182, 5);

            graphics.blit(ModTextures.CANNON_ICONS_TEXTURE, i, l, 0, 5, k, 5);


            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, -90);

            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int w = 9;
            graphics.blit(ModTextures.CANNON_ICONS_TEXTURE, (screenWidth - w) / 2, (screenHeight - w) / 2,
                    0, 10, w, w);

            RenderSystem.defaultBlendFunc();


            graphics.pose().popPose();
        }
    }

    public void setupOverlayRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }
}