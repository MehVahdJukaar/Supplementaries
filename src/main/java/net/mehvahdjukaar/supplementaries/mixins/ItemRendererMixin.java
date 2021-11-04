package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.items.SlingshotRendererHelper;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Shadow
    public float blitOffset;

    @Shadow
    public abstract void render(ItemStack stack, ItemTransforms.TransformType transform, boolean leftHand, PoseStack matrixStack, MultiBufferSource buffer, int light, int overlay, BakedModel model);

    @Shadow
    public abstract BakedModel getModel(ItemStack p_174265_, @Nullable Level p_174266_, @Nullable LivingEntity p_174267_, int p_174268_);

    @Inject(
            method = "renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At(value = "RETURN")
    )
    private void renderInGui(ItemStack stack, int x, int y, BakedModel model, CallbackInfo ci) {
        if (!stack.isEmpty() && stack.getItem() == ModRegistry.SLINGSHOT_ITEM.get()) {
            boolean overlay = ClientConfigs.cached.SLINGSHOT_OVERLAY;
            boolean outline = ClientConfigs.cached.SLINGSHOT_OUTLINE;
            if (overlay || outline) {
                LocalPlayer player = Minecraft.getInstance().player;

                if (player != null && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {

                    if (overlay) {
                        ItemStack ammo = SlingshotRendererHelper.getAmmoForPreview(stack, Minecraft.getInstance().level, player);

                        if (!ammo.isEmpty()) {

                            PoseStack posestack = RenderSystem.getModelViewStack();
                            posestack.pushPose();


                            posestack.translate(16.0F * (-0.25D) + (8.0F + x) * (1 - 0.4f),
                                    16.0F * (0.25D + 0.025) + (8.0F + y) * (1 - 0.4f),
                                    16.0F + (100.0F + this.blitOffset) * (1 - 0.4f));
                            posestack.scale(0.4f, 0.4f, 0.4f);

                            //0.4 scale
                            RenderSystem.applyModelViewMatrix();

                            Minecraft.getInstance().getItemRenderer().renderGuiItem(ammo, x, y);

                            posestack.popPose();
                            RenderSystem.applyModelViewMatrix();
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


}