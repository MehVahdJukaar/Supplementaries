package net.mehvahdjukaar.supplementaries.client.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.models.HatStandModel;
import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class HatStandRenderer extends LivingEntityRenderer<HatStandEntity, HatStandModel> {


    public HatStandRenderer(EntityRendererProvider.Context context) {
        super(context, new HatStandModel(context.bakeLayer(ClientRegistry.HAT_STAND_MODEL)), 0);
        ModelPart modelPart = context.bakeLayer(ClientRegistry.HAT_STAND_MODEL_ARMOR);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(modelPart),
                new HumanoidModel<>(modelPart), context.getModelManager()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
    }

    @Override
    protected void setupRotations(HatStandEntity entityLiving, PoseStack matrixStack, float ageInTicks, float rotationYaw, float partialTicks) {
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - rotationYaw));
        float f = (entityLiving.level().getGameTime() - entityLiving.lastHit) + partialTicks;
        if (f < 5.0F) {
            matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(f / 1.5F * 3.1415927F) * 3.0F));
        }

    }

    @Override
    public Vec3 getRenderOffset(HatStandEntity entity, float partialTicks) {
        if(entity.isNoBasePlate())return new Vec3(0,-1/16f,0);
        return super.getRenderOffset(entity, partialTicks);
    }

    @Override
    protected boolean shouldShowName(HatStandEntity entity) {
        double d = this.entityRenderDispatcher.distanceToSqr(entity);
        float f = entity.isCrouching() ? 32.0F : 64.0F;
        return (d < (f * f)) && entity.isCustomNameVisible();
    }

    @Override
    public ResourceLocation getTextureLocation(HatStandEntity entity) {
        return ModTextures.HAT_STAND;
    }
}
