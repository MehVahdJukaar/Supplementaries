package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.layers.PlundererItemInHandRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.PlundererModel;
import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.resources.ResourceLocation;

public class PlundererRenderer extends IllagerRenderer<PlundererEntity> {

    public PlundererRenderer(EntityRendererProvider.Context context) {
        super(context, new PlundererModel(context.bakeLayer(ClientRegistry.PLUNDERER_MODEL)), 0.5F);
        this.addLayer(new PlundererItemInHandRenderer<>(this, context.getItemInHandRenderer()));
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

