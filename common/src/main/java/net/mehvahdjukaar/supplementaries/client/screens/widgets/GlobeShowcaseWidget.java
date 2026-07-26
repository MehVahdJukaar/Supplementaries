package net.mehvahdjukaar.supplementaries.client.screens.widgets;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.gui.ConfigScreenExtensions;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GlobeBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

public class GlobeShowcaseWidget extends AbstractWidget {

    public static final ConfigScreenExtensions.Showcase SHOWCASE = new ConfigScreenExtensions.Showcase() {
        @Override
        public AbstractWidget create(String modId, int x, int y, int width, int maxHeight) {
            return new GlobeShowcaseWidget(x, y, width, maxHeight);
        }

        @Override
        public boolean replacesCarousel() {
            return false;
        }
    };

    // the display transform every block model gets in an inventory slot, so it's seen from the same angle
    private static final float GUI_TILT = 30f;
    private static final float GUI_YAW = 225f;
    private static final float GUI_SCALE = 0.625f;
    private static final float SLOT_FILL = 1.35f;       // how much bigger than an inventory slot it's drawn at
    private static final float MODEL_CENTER_Y = 0.4375f; // stand plus globe is 14px tall, not the full block
    private static final float DEG_PER_PIXEL = 2f;
    private static final float SECONDS_PER_TICK = 0.05f;
    private static final int DRAG_SLOP = 3;             // px of movement that still counts as a click

    private final GlobeBlockTile tile;
    private final RandomSource random = RandomSource.create();

    private float dragYaw;
    private float dragged;      // how far this press has moved, so a drag doesn't also spin it
    private float tickTimer;
    private long lastMs = -1;

    public GlobeShowcaseWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.translatable("block.supplementaries.globe"));
        this.tile = new GlobeBlockTile(BlockPos.ZERO, ModRegistry.GLOBE.get().defaultBlockState());
        // its own world, seeded off the mod version, so it changes with every release
        String version = PlatHelper.getModVersion(Supplementaries.MOD_ID);
        this.tile.setRenderData(GlobeManager.seededRenderData(version == null ? 0 : version.hashCode()));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        BlockEntityRenderer<GlobeBlockTile> renderer = Minecraft.getInstance()
                .getBlockEntityRenderDispatcher().getRenderer(this.tile);
        if (renderer == null) return;

        float slot = Math.min(this.width, this.height) * SLOT_FILL;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(this.getX() + this.width / 2f, this.getY() + this.height / 2f, 150);
        // same chain an item goes through in a slot: the negative y cancels out the gui projection's own flip, so the
        // quads keep their winding and the culled block render types don't turn inside out
        pose.scale(slot, -slot, slot);
        pose.mulPose(Axis.XP.rotationDegrees(GUI_TILT));
        pose.mulPose(Axis.YP.rotationDegrees(GUI_YAW + this.dragYaw));
        pose.scale(GUI_SCALE, GUI_SCALE, GUI_SCALE);
        pose.translate(-0.5f, -MODEL_CENTER_Y, -0.5f);   // both renderers start from the block corner

        Lighting.setupFor3DItems();
        MultiBufferSource.BufferSource buffer = graphics.bufferSource();
        // the stand is the block's own model, only the globe on top of it belongs to the block entity
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(this.tile.getBlockState(), pose, buffer,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        renderer.render(this.tile, this.advance(), pose, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        graphics.flush();
        pose.popPose();
    }

    /**
     * Runs the tile's spin decay off real time, since there is no level ticking it. @return the leftover partial tick
     */
    private float advance() {
        long now = Util.getMillis();
        float dt = this.lastMs < 0 ? 0 : Math.min((now - this.lastMs) / 1000f, 0.1f); // clamp screen-reopen gaps
        this.lastMs = now;
        this.tickTimer += dt;
        while (this.tickTimer >= SECONDS_PER_TICK) {
            this.tickTimer -= SECONDS_PER_TICK;
            this.tile.decaySpin();
        }
        return this.tickTimer / SECONDS_PER_TICK;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.dragged = 0;
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        this.dragYaw += (float) dragX * DEG_PER_PIXEL;
        this.dragged += Math.abs((float) dragX) + Math.abs((float) dragY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (this.dragged > DRAG_SLOP) return;
        this.tile.spin();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.GLOBE_SPIN.get(),
                MthUtils.nextWeighted(this.random, 0.2f) + 0.9f, 0.65f));
    }

    @Override
    public void playDownSound(SoundManager handler) {
        // the spin sound plays on release instead, so dragging stays silent
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }
}
