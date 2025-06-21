package net.mehvahdjukaar.supplementaries.client.cannon;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.common.block.cannon.CannonAccess;
import net.mehvahdjukaar.supplementaries.common.block.cannon.ShootingMode;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CannonChargeHud implements LayeredDraw.Layer {

    public static final CannonChargeHud INSTANCE = new CannonChargeHud();
    protected final Minecraft mc;

    protected CannonChargeHud() {
        this.mc = Minecraft.getInstance();
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (mc.options.hideGui) return;
        if (CannonController.isActive()) {

            CannonBlockTile cannon = CannonController.access.getInternalCannon();

            setupOverlayRenderState();
            int screenWidth = graphics.guiWidth();
            int screenHeight = graphics.guiHeight();

            renderHotBar(graphics, screenWidth, screenHeight, cannon);

            renderCrossHair(graphics, screenWidth, screenHeight);

            renderBar(graphics, screenWidth, screenHeight, cannon, deltaTracker.getGameTimeDeltaPartialTick(false));

            renderTrajectoryIcons(graphics, screenWidth, screenHeight);

        } else if (mc.player.getVehicle() instanceof CannonAccess be) {
            setupOverlayRenderState();
            int screenWidth = graphics.guiWidth();
            int screenHeight = graphics.guiHeight();
            renderBar(graphics, screenWidth, screenHeight, be.getInternalCannon(), deltaTracker.getGameTimeDeltaPartialTick(false));
        }
    }

    private static void renderTrajectoryIcons(GuiGraphics graphics, int screenWidth, int screenHeight) {
        // trajectory icons

        int iconLeft = screenWidth / 2 + 96;
        int iconTop = screenHeight - 22;
        int iconW = 14;
        ResourceLocation tr = switch (CannonController.shootingMode.ordinal()) {
            case 0 -> ModTextures.CANNON_TRAJECTORY_0_SPRITE;
            case 1 -> ModTextures.CANNON_TRAJECTORY_1_SPRITE;
            default -> ModTextures.CANNON_TRAJECTORY_2_SPRITE;
        };
        graphics.blitSprite(tr, iconLeft, iconTop, iconW, iconW);

        iconLeft = screenWidth / 2 - (96 + 14);
        ResourceLocation tr2 = CannonController.showsTrajectory ? ModTextures.CANNON_TRAJECTORY_SHOWN_SPRITE : ModTextures.CANNON_TRAJECTORY_HIDDEN_SPRITE;
        graphics.blitSprite(tr2, iconLeft, iconTop, iconW, iconW);
    }

    private void renderHotBar(GuiGraphics graphics, int screenWidth, int screenHeight, CannonBlockTile cannon) {
        int left = screenWidth / 2 - 91;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, -90.0F);
        graphics.blitSprite(ModTextures.CANNON_HOTBAR_SPRITE, left, screenHeight - 22, 182, 22);

        graphics.pose().popPose();
        Player player = Minecraft.getInstance().player;
        int yPos = screenHeight - 16 - 3;
        this.renderSlot(graphics, left + 1 + 47 + 2, yPos, player, cannon.getProjectile(), 1);
        this.renderSlot(graphics, left + 1 + 113 + 2, yPos, player, cannon.getFuel(), 1);
    }

    private void renderBar(GuiGraphics graphics, int screenWidth, int screenHeight, CannonBlockTile cannon,
                           float partialTicks) {
        int xpBarLeft = screenWidth / 2 - 91;

        float c = 1 - cannon.getCooldownAnimation(partialTicks);
        int k = (int) (c * 183.0F);
        int xpBarTop = screenHeight - 32 + 3;
        graphics.blitSprite(ModTextures.CANNON_CHARGE_BACKGROUND_SPRITE, xpBarLeft, xpBarTop, 182, 5);
        float f = cannon.getFiringAnimation(partialTicks);

        float min = 0.7F;

        if (f > 0) {
            f = 1 - f;
            float red = Math.min(f * 0.4F + min,1);

            float green = Math.min(min - f * 0.4f * min,1);
            float blue = min;

            RenderSystem.setShaderColor(red, green, min - f * blue, 1.0F);
        } else {
            RenderSystem.setShaderColor(min, min, min, 1.0F);
        }

        graphics.blitSprite(ModTextures.CANNON_CHARGE_PROGRESS_SPRITE, 183, 5,
                0, 0, xpBarLeft, xpBarTop, k, 5);


        byte power = cannon.getPowerLevel();

        int color = switch (power) {
            case 2 -> 0xffaa00;
            case 3 -> 0xff8800;
            case 4 -> 0xff6600;
            default -> 0xffcc00;
        };

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        String s = String.valueOf(power);
        int i1 = (screenWidth - mc.font.width(s)) / 2;
        int j1 = screenHeight - 31 - 4;
        graphics.drawString(mc.font, s, i1 + 1, j1, 0, false);
        graphics.drawString(mc.font, s, i1 - 1, j1, 0, false);
        graphics.drawString(mc.font, s, i1, j1 + 1, 0, false);
        graphics.drawString(mc.font, s, i1, j1 - 1, 0, false);
        graphics.drawString(mc.font, s, i1, j1, color, false);
    }

    private static void renderCrossHair(GuiGraphics graphics, int screenWidth, int screenHeight) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, -90);

        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        int w = 9;
        ResourceLocation hitType;
        if (CannonController.shootingMode == ShootingMode.STRAIGHT) {
            hitType = ModTextures.CANNON_CROSSHAIR_AIM_SPRITE;
        } else if (CannonController.trajectory == null || CannonController.trajectory.miss()) {
            hitType = ModTextures.CANNON_CROSSHAIR_MISS_SPRITE;
        } else hitType = ModTextures.CANNON_CROSSHAIR_HIT_SPRITE;

        graphics.blitSprite(hitType, (screenWidth - w) / 2, (screenHeight - w) / 2, w, w);

        RenderSystem.defaultBlendFunc();

        graphics.pose().popPose();
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
            guiGraphics.renderItemDecorations(this.mc.font, itemStack, x, y);
        }
    }


}