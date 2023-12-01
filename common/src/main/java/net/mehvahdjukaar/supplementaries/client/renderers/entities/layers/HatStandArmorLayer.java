package net.mehvahdjukaar.supplementaries.client.renderers.entities.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.entity.EquipmentSlot;

public class HatStandArmorLayer<T extends HatStandEntity, A extends HumanoidModel<T>> extends HumanoidArmorLayer<T, A, A> {

    public HatStandArmorLayer(RenderLayerParent<T, A> renderLayerParent, A modelHelmet, ModelManager modelManager) {
        super(renderLayerParent, modelHelmet, modelHelmet, modelManager);
    }

    @Override
    public void setPartVisibility(A modelIn, EquipmentSlot slotIn) {
        modelIn.setAllVisible(false);
        if(slotIn == EquipmentSlot.HEAD){
            modelIn.head.visible = true;
            modelIn.hat.visible = true;
        }
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        super.render(matrixStack, buffer, packedLight, livingEntity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    }
}