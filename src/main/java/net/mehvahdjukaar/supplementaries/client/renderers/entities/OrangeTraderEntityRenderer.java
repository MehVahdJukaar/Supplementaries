package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.entities.RedMerchantEntity;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;

public class OrangeTraderEntityRenderer extends MobRenderer<RedMerchantEntity, OrangeTraderModel<RedMerchantEntity>> {

    public OrangeTraderEntityRenderer(EntityRenderDispatcher manager) {
        super(manager, new OrangeTraderModel<>(0.0F), 0.5F);
        this.addLayer(new CustomHeadLayer<>(this));
        this.addLayer(new CrossedArmsItemLayer<>(this));
        this.getModel().getHead().y = this.getModel().getHead().y + 1 - 0.9375F;
    }

    @Override
    public ResourceLocation getTextureLocation(RedMerchantEntity entity) {
        return Textures.RED_MERCHANT;
    }

    @Override
    protected void scale(RedMerchantEntity entity, PoseStack matrixStack, float ticks) {
        matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
