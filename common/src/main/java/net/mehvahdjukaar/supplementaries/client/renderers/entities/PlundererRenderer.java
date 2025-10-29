package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class PlundererRenderer extends IllagerRenderer<PlundererEntity> {

    public PlundererRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.PILLAGER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    /**
     * Returns the location of an entity's texture.
     */
    @Override
    public ResourceLocation getTextureLocation(PlundererEntity entity) {
        return ModTextures.PLUNDERER;
    }

    public static LayerDefinition createMesh() {
        return IllagerModel.createBodyLayer();
    }
}

