package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.HatStandModel;
import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class HatStandRenderer extends LivingEntityRenderer<HatStandEntity, HatStandModel> {


    public HatStandRenderer(EntityRendererProvider.Context context) {
        super(context, new HatStandModel(context.bakeLayer(ClientRegistry.HAT_STAND_MODEL)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(HatStandEntity entity) {
        return ModTextures.HAT_STAND;
    }
}
