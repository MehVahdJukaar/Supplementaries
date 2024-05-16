package net.mehvahdjukaar.supplementaries.client.cannon;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class CannonChargeOverlay extends Gui {

    private final Minecraft minecraft;

    protected CannonChargeOverlay(Minecraft minecraft, ItemRenderer itemRenderer) {
        super(minecraft, itemRenderer);
        this.minecraft = minecraft;
    }

    public void renderBar(GuiGraphics graphics, float partialTicks, int screenWidth, int screenHeight) {
        if (!minecraft.options.hideGui && CannonController.isActive()) {

            setupOverlayRenderState();
            ResourceLocation texture = ModTextures.CANNON_ICONS_TEXTURE;
            CannonBlockTile cannon = CannonController.cannon;


            int left = screenWidth / 2 - 91;
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, -90.0F);
            graphics.blit(texture, left, screenHeight - 22, 0, 19, 182, 22);

            graphics.pose().popPose();
            Player player = Minecraft.getInstance().player;
            int k2;
            k2 = screenHeight - 16 - 3;
            this.renderSlot(graphics, left + 1 + 47 + 2, k2, player, cannon.getProjectile(), 1);
            this.renderSlot(graphics, left + 1 + 113 + 2, k2, player, cannon.getFuel(), 1);


            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, -90);

            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int w = 9;
            int hitType = CannonController.trajectory != null && !CannonController.trajectory.miss() ?
                    0 : 18;
            graphics.blit(texture, (screenWidth - w) / 2, (screenHeight - w) / 2,
                    hitType, 10, w, w);

            RenderSystem.defaultBlendFunc();


            graphics.pose().popPose();

            int i = screenWidth / 2 - 91;

            float c = 1 - cannon.getDisabledCooldown();
            int k = (int) (c * 183.0F);
            int l = screenHeight - 32 + 3;
            graphics.blit(texture, i, l, 0, 0, 182, 5);
            float f = cannon.getFireTimer();

            float min = 0.7F;

            if (f > 0) {
                f = 1 - f;
                float red = f * 0.4F + min;

                float green = min - f * 0.4f * min;
                float blue = min;

                RenderSystem.setShaderColor(red, green, min - f * blue, 1.0F);
            } else {
                RenderSystem.setShaderColor(min, min, min, 1.0F);
            }

            graphics.blit(texture, i, l, 0, 5, k, 5);


            int power = CannonController.cannon.getFirePower();

            int color = switch (power) {
                default -> 0xffcc00;
                case 2 -> 0xffaa00;
                case 3 -> 0xff8800;
                case 4 -> 0xff6600;
            };

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            String s = String.valueOf(power);
            int i1 = (screenWidth - this.getFont().width(s)) / 2;
            int j1 = screenHeight - 31 - 4;
            graphics.drawString(this.getFont(), s, i1 + 1, j1, 0, false);
            graphics.drawString(this.getFont(), s, i1 - 1, j1, 0, false);
            graphics.drawString(this.getFont(), s, i1, j1 + 1, 0, false);
            graphics.drawString(this.getFont(), s, i1, j1 - 1, 0, false);
            graphics.drawString(this.getFont(), s, i1, j1, color, false);

        }
    }

    public void setupOverlayRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    private void renderSlot(GuiGraphics guiGraphics, int x, int y, Player player, ItemStack itemStack, int seed) {
        if (!itemStack.isEmpty()) {
            guiGraphics.renderItem(player, itemStack, x, y, seed);
            guiGraphics.renderItemDecorations(this.minecraft.font, itemStack, x, y);
        }
    }

}