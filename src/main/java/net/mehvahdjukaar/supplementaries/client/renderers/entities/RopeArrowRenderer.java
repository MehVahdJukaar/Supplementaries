package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import net.mehvahdjukaar.supplementaries.client.renderers.entities.pickle.PicklePlayer;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.common.entities.RopeArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class RopeArrowRenderer extends ArrowRenderer<RopeArrowEntity> {

    public RopeArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        PicklePlayer.createRenderInstance(context);
    }

    @Override
    public ResourceLocation getTextureLocation(RopeArrowEntity entity) {
        return Textures.ROPE_ARROW;
    }

}
