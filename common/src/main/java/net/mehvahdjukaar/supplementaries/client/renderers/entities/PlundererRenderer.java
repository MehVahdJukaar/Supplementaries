package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.PlundererModel;
import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class PlundererRenderer extends IllagerRenderer<PlundererEntity> {

    public PlundererRenderer(EntityRendererProvider.Context context) {
        super(context, new PlundererModel(context.bakeLayer(ClientRegistry.PLUNDERER_MODEL)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()) {
            @Override
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlundererEntity vindicator, float f, float g, float h, float j, float k, float l) {
                if (vindicator.isAggressive()) {
                    super.render(poseStack, multiBufferSource, i, vindicator, f, g, h, j, k, l);
                }
            }
        });
    }


    @Override
    public void render(PlundererEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model.getHat().visible = true;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * Returns the location of an entity's texture.
     */
    @Override
    public ResourceLocation getTextureLocation(PlundererEntity entity) {
        return ModTextures.PLUNDERER;
    }

}

