package net.mehvahdjukaar.supplementaries.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.common.entities.ISlimeable;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

public abstract class SlimedOverlayHud {
    protected final Minecraft mc;

    protected SlimedOverlayHud(Minecraft minecraft) {
        this.mc = minecraft;
    }

    protected void render(GuiGraphics graphics, float partialTicks, int screenWidth, int screenHeight) {

        float alpha = ISlimeable.getAlpha(mc.player, partialTicks);
        if (alpha > 0) {

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
            int slide = (int) (screenHeight / 4f * (1 - alpha));
            graphics.blit(ModTextures.SLIME_GUI_OVERLAY, 0, 0, -90, 0.0F, 0.0F,
                    screenWidth, screenHeight + slide, screenWidth, screenHeight + slide);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }


}
