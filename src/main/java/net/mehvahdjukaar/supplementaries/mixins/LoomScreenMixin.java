package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.tiles.FlagBlockTileRenderer;
import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.minecraft.block.BannerBlock;
import net.minecraft.client.gui.screen.LoomScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.LoomContainer;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin extends ContainerScreen<LoomContainer> {

    @Shadow
    private List<Pair<BannerPattern, DyeColor>> resultBannerPatterns;
    @Shadow
    private boolean hasMaxPatterns;
    @Shadow
    private ItemStack bannerStack;

    public LoomScreenMixin(LoomContainer p_i51105_1_, PlayerInventory p_i51105_2_, ITextComponent p_i51105_3_) {
        super(p_i51105_1_, p_i51105_2_, p_i51105_3_);
    }

    @Redirect(method = "containerChanged",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getItem()Lnet/minecraft/item/Item;",
                    ordinal = 0))
    public Item containerChanged(ItemStack stack) {
        Item i = stack.getItem();
        if (i instanceof FlagItem) {
            i = BannerBlock.byColor(((FlagItem) i).getColor()).asItem();
        }
        return i;
    }


    @Inject(method = "renderBg", at = @At("TAIL"))
    public void renderBg(MatrixStack matrixStack, float ticks, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.resultBannerPatterns != null && !this.hasMaxPatterns && this.bannerStack.getItem() instanceof FlagItem) {
            int i = this.leftPos;
            int j = this.topPos;
            IRenderTypeBuffer.Impl renderTypeBuffer = this.minecraft.renderBuffers().bufferSource();
            matrixStack.pushPose();

            matrixStack.translate(i + 139, j + 52, 0.0D);
            matrixStack.scale(24.0F, -24.0F, 1.0F);
            matrixStack.translate(0.5D, 0.5D, 0.5D);
            matrixStack.mulPose(Const.Y90);
            matrixStack.mulPose(Const.X90);
            matrixStack.scale(1.125F, 1.125F, 1.125F);
            matrixStack.translate(-1, -0.5, -1.1875);

            //matrixStack.translate(ClientConfigs.general.TEST1.get(),ClientConfigs.general.TEST2.get(),ClientConfigs.general.TEST3.get());

            FlagBlockTileRenderer.renderPatterns(matrixStack, renderTypeBuffer, this.resultBannerPatterns, 15728880);

            matrixStack.popPose();
            renderTypeBuffer.endBatch();
        }
    }
}
