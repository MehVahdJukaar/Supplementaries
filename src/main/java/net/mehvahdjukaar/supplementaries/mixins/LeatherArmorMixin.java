package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.LeatherPatternTexturesRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class LeatherArmorMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {


    @Shadow
    protected abstract void renderModel(PoseStack p_117107_, MultiBufferSource p_117108_, int p_117109_, boolean p_117111_, Model p_117112_, float p_117114_, float p_117115_, float p_117116_, ResourceLocation armorResource);

    public LeatherArmorMixin(RenderLayerParent<T, M> pRenderer) {
        super(pRenderer);
    }

    @Inject(method = "renderArmorPiece", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IZLnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V",
            ordinal = 0),
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=overlay"
                    )
            ))
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, T entity, EquipmentSlot equipmentSlot,
                       int packedLight, A model, CallbackInfo ci) {
        if (equipmentSlot == EquipmentSlot.CHEST) {
            ItemStack itemStack = entity.getItemBySlot(equipmentSlot);
            ResourceLocation texture = LeatherPatternTexturesRegistry.getTexture(itemStack);
            if (texture != null) {
                this.renderModel(poseStack, bufferSource, packedLight, itemStack.hasFoil(), model, 1.0F, 1.0F, 1.0F, texture);
            }
        }
    }


}
