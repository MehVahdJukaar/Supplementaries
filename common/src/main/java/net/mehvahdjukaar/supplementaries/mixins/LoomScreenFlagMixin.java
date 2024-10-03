package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.FlagBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoomScreen.class)
public abstract class LoomScreenFlagMixin extends AbstractContainerScreen<LoomMenu> {

    @Unique
    private final CyclingSlotBackground supplementaries$bannerFlagBG = new CyclingSlotBackground(0);

    @Shadow
    private boolean hasMaxPatterns;
    @Shadow
    private ItemStack bannerStack;


    @Shadow @Nullable
    private BannerPatternLayers resultBannerPatterns;

    protected LoomScreenFlagMixin(LoomMenu loomMenu, Inventory inventory, Component component) {
        super(loomMenu, inventory, component);
    }


    @Inject(method = "renderBg", at = @At("TAIL"))
    public void supp$renderFlags(GuiGraphics graphics, float ticks, int mouseX, int mouseY, CallbackInfo ci) {
        this.supplementaries$bannerFlagBG.render(this.menu, graphics, ticks, this.leftPos, this.topPos);

        if (this.resultBannerPatterns != null && !this.hasMaxPatterns && this.bannerStack.getItem() instanceof FlagItem fi) {
            int i = this.leftPos;
            int j = this.topPos;
            MultiBufferSource.BufferSource renderTypeBuffer = this.minecraft.renderBuffers().bufferSource();
            PoseStack pose = graphics.pose();
            pose.pushPose();

            pose.translate(i + 139d, j + 52d, 0.0D);
            pose.scale(24.0F, -24.0F, 1.0F);
            pose.translate(0.5D, 0.5D, 0.5D);
            pose.mulPose(RotHlpr.Y90);
            pose.mulPose(RotHlpr.X90);
            pose.scale(1.125F, 1.125F, 1.125F);
            pose.translate(-1, -0.5, -1.1875);
            Lighting.setupForFlatItems();

            FlagBlockTileRenderer.renderPatterns(pose, renderTypeBuffer, this.resultBannerPatterns,
                    15728880, fi.getColor());

            pose.popPose();
            renderTypeBuffer.endBatch();

            Lighting.setupFor3DItems();
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.supplementaries$bannerFlagBG.tick(ModTextures.BANNER_SLOT_ICONS);
    }
}
