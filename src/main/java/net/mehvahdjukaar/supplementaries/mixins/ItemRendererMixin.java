package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.supplementaries.client.renderers.items.SlingshotRendererHelper;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
    public abstract void render(ItemStack stack, ItemTransforms.TransformType transform, boolean leftHand, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, BakedModel model);

    @Shadow
    public abstract BakedModel getModel(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entity);

    @Inject(
            method = "renderGuiItem(Lnet/minecraft/item/ItemStack;IILnet/minecraft/client/renderer/model/IBakedModel;)V",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    private void renderInGui(ItemStack stack, int x, int y, BakedModel model, CallbackInfo ci) {
        if (!stack.isEmpty() && stack.getItem() == ModRegistry.SLINGSHOT_ITEM.get()){
            boolean overlay = ClientConfigs.cached.SLINGSHOT_OVERLAY;
            boolean outline = ClientConfigs.cached.SLINGSHOT_OUTLINE;
            if(overlay || outline){
                LocalPlayer player = Minecraft.getInstance().player;

                if (player != null && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {

                    if(overlay) {
                        ItemStack ammo = SlingshotRendererHelper.getAmmoForPreview(stack, Minecraft.getInstance().level, player);

                        if (!ammo.isEmpty()) {

                            this.renderSlingshotOverlay(ammo, x, y);
                        }
                    }
                    if(outline){
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
        BakedModel iBakedModel = this.getModel(ammo, null, Minecraft.getInstance().player);

        RenderSystem.pushMatrix();
        this.textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
        this.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
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
        PoseStack matrixStack = new PoseStack();
        matrixStack.translate(-0.25D, -0.25D - 0.025, 1.0D);
        matrixStack.scale(0.4F, 0.4F, 0.4F);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean is2D = !iBakedModel.usesBlockLight();
        if (is2D) {
            Lighting.setupForFlatItems();
        }

        this.render(ammo, ItemTransforms.TransformType.GUI, false, matrixStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, iBakedModel);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();

        if (is2D) {
            Lighting.setupFor3DItems();
        }

        RenderSystem.disableAlphaTest();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
    }


}