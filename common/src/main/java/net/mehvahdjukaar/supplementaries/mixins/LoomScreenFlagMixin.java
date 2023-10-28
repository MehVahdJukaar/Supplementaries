package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.FlagBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.common.items.FlagItem;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LoomScreen.class)
public abstract class LoomScreenFlagMixin extends AbstractContainerScreen<LoomMenu> {

    @Unique
    private final CyclingSlotBackground supplementaries$bannerFlagBG = new CyclingSlotBackground(0);

    @Shadow
    private List<Pair<Holder<BannerPattern>, DyeColor>> resultBannerPatterns;
    @Shadow
    private boolean hasMaxPatterns;
    @Shadow
    private ItemStack bannerStack;


    protected LoomScreenFlagMixin(LoomMenu loomMenu, Inventory inventory, Component component) {
        super(loomMenu, inventory, component);
    }

    //TODO: test
    @WrapOperation(method = "containerChanged",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",
                    ordinal = 0))
    public Item containerChanged(ItemStack itemStack, Operation<Item> isItem) {
        Item i = itemStack.getItem();
        if (i instanceof FlagItem fi) {
            //hax
            return BannerBlock.byColor(fi.getColor()).asItem();
        }
        return isItem.call(itemStack);
    }


    @Inject(method = "renderBg", at = @At("TAIL"))
    public void renderBg(GuiGraphics graphics, float ticks, int mouseX, int mouseY, CallbackInfo ci) {
        this.supplementaries$bannerFlagBG.render(this.menu, graphics, ticks, this.leftPos, this.topPos);

        if (this.resultBannerPatterns != null && !this.hasMaxPatterns && this.bannerStack.getItem() instanceof FlagItem) {
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

            FlagBlockTileRenderer.renderPatterns(pose, renderTypeBuffer, this.resultBannerPatterns, 15728880);

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
