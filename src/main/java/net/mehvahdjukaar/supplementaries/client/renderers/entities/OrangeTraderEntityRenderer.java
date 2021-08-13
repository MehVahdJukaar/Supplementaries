package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.entities.OrangeMerchantEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.entity.merchant.villager.WanderingTraderEntity;
import net.minecraft.util.ResourceLocation;

public class OrangeTraderEntityRenderer extends MobRenderer<OrangeMerchantEntity, OrangeTraderModel<OrangeMerchantEntity>> {

    public OrangeTraderEntityRenderer(EntityRendererManager manager) {
        super(manager, new OrangeTraderModel<>(0.0F), 0.5F);
        this.addLayer(new HeadLayer<>(this));
        this.addLayer(new CrossedArmsItemLayer<>(this));
        this.getModel().getHead().y = this.getModel().getHead().y + 1 - 0.9375F;
    }

    public ResourceLocation getTextureLocation(OrangeMerchantEntity entity) {
        return Textures.ORANGE_TRADER;
    }

    protected void scale(WanderingTraderEntity entity, MatrixStack matrixStack, float ticks) {
        matrixStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
