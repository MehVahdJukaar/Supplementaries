package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.client.renderers.items.SlingshotRendererHelper;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Final
    @Shadow
    private TextureManager textureManager;

    @Shadow
    public float blitOffset;

    @Shadow
    public abstract void render(ItemStack stack, ItemCameraTransforms.TransformType transform, boolean leftHand, MatrixStack matrixStack, IRenderTypeBuffer buffer, int light, int overlay, IBakedModel model);

    @Shadow
    public abstract IBakedModel getModel(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity);

    @Inject(
            method = "renderGuiItem(Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/renderer/model/IBakedModel;)V",
            at = @At(value = "RETURN")
    )
    private void renderInGui(ItemStack stack, int x, int y, IBakedModel model, CallbackInfo ci) {
        if (!stack.isEmpty() && stack.getItem() == ModRegistry.SLINGSHOT_ITEM.get()) {
            boolean overlay = ClientConfigs.cached.SLINGSHOT_OVERLAY;
            boolean outline = ClientConfigs.cached.SLINGSHOT_OUTLINE;
            if (overlay || outline) {
                ClientPlayerEntity player = Minecraft.getInstance().player;

                if (player != null && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {

                    if (overlay) {
                        ItemStack ammo = SlingshotRendererHelper.getAmmoForPreview(stack, Minecraft.getInstance().level, player);

                        if (!ammo.isEmpty()) {

                            this.renderSlingshotOverlay(ammo, x, y);
                        }
                    }
                    if (outline) {
                        if (EnchantmentHelper.getItemEnchantmentLevel(ModRegistry.STASIS_ENCHANTMENT.get(), stack) != 0) {
                            SlingshotRendererHelper.grabNewLookPos(player);
                        }
                    }
                }
            }
        }
    }

    //todo: maybe move this out of here
    private void renderSlingshotOverlay(ItemStack ammo, int x, int y) {
        IBakedModel iBakedModel = this.getModel(ammo, null, Minecraft.getInstance().player);

        RenderSystem.pushMatrix();
        this.textureManager.bind(AtlasTexture.LOCATION_BLOCKS);
        this.textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.enableRescaleNormal();
        RenderSystem.enableAlphaTest();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.translatef((float) x, (float) y, 100.0F + this.blitOffset);
        RenderSystem.translatef(8.0F, 8.0F, 0.0F);
        RenderSystem.scalef(1.0F, -1.0F, 1.0F);
        RenderSystem.scalef(16.0F, 16.0F, 16.0F);
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.translate(-0.25D, -0.25D - 0.025, 1.0D);
        matrixStack.scale(0.4F, 0.4F, 0.4F);

        IRenderTypeBuffer.Impl bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean is2D = !iBakedModel.usesBlockLight();
        if (is2D) {
            RenderHelper.setupForFlatItems();
        }

        this.render(ammo, ItemCameraTransforms.TransformType.GUI, false, matrixStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, iBakedModel);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();

        if (is2D) {
            RenderHelper.setupFor3DItems();
        }

        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
    }


}