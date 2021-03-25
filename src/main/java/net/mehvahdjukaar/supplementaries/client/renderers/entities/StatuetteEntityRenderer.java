package net.mehvahdjukaar.supplementaries.client.renderers.entities;


import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public abstract class StatuetteEntityRenderer extends EntityRenderer<AbstractClientPlayerEntity> implements IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
    protected PlayerModel<AbstractClientPlayerEntity> entityModel;
    protected final List<LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> layerRenderers = Lists.newArrayList();

    public StatuetteEntityRenderer(EntityRendererManager rendererManager, boolean useSmallArms) {
        super(rendererManager);
        this.entityModel = new PlayerModel<>(0.0F, useSmallArms);
        this.layerRenderers.add(new HeldItemLayer<>(this));
        this.layerRenderers.add(new Deadmau5HeadLayer(this));
        this.layerRenderers.add(new CapeLayer(this));
        this.layerRenderers.add(new HeadLayer<>(this));
    }

    public PlayerModel<AbstractClientPlayerEntity> getModel() {
        return this.entityModel;
    }

    public void render(AbstractClientPlayerEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();

        float f = MathHelper.rotLerp(partialTicks, entityIn.yBodyRotO, entityIn.yBodyRot);
        float f1 = MathHelper.rotLerp(partialTicks, entityIn.yHeadRotO, entityIn.yHeadRot);
        float f2 = f1 - f;

        float f6 = MathHelper.lerp(partialTicks, entityIn.xRotO, entityIn.xRot);

        float f7 = this.handleRotationFloat(entityIn, partialTicks);
        this.applyRotations(entityIn, matrixStackIn, f7, f, partialTicks);
        matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
        //player scale
        matrixStackIn.scale(0.9375F, 0.9375F, 0.9375F);
        matrixStackIn.translate(0.0D, -1.501F, 0.0D);
        float f8 = 0.0F;
        float f5 = 0.0F;
        if (entityIn.isAlive()) {
            f8 = MathHelper.lerp(partialTicks, entityIn.animationSpeedOld, entityIn.animationSpeed);
            f5 = entityIn.animationPosition - entityIn.animationSpeed * (1.0F - partialTicks);
            if (f8 > 1.0F) {
                f8 = 1.0F;
            }
        }

        this.entityModel.prepareMobModel(entityIn, f5, f8, partialTicks);
        this.entityModel.setupAnim(entityIn, f5, f8, f7, f2, f6);

        RenderType rendertype = this.entityModel.renderType(this.getTextureLocation(entityIn));
        if (rendertype != null) {
            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(rendertype);
            this.entityModel.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, 0, 1.0F, 1.0F, 1.0F, 1.0F);
        }


        for(LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> layerrenderer : this.layerRenderers) {
            layerrenderer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, f5, f8, partialTicks, f7, f2, f6);
        }


        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }


    /**
     * Defines what float the third param in setRotationAngles of ModelBase is
     */
    protected float handleRotationFloat(AbstractClientPlayerEntity livingBase, float partialTicks) {
        return (float)livingBase.tickCount + partialTicks;
    }

    protected boolean shouldShowName(AbstractClientPlayerEntity entity) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        float f = entity.isDiscrete() ? 32.0F : 64.0F;
        if (d0 >= (double)(f * f)) {
            return false;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            ClientPlayerEntity clientplayerentity = minecraft.player;
            boolean flag = !entity.isInvisibleTo(clientplayerentity);
            if (entity != clientplayerentity) {
                Team team = entity.getTeam();
                Team team1 = clientplayerentity.getTeam();
                if (team != null) {
                    Team.Visible team$visible = team.getNameTagVisibility();
                    switch(team$visible) {
                        case ALWAYS:
                            return flag;
                        case NEVER:
                            return false;
                        case HIDE_FOR_OTHER_TEAMS:
                            return team1 == null ? flag : team.isAlliedTo(team1) && (team.canSeeFriendlyInvisibles() || flag);
                        case HIDE_FOR_OWN_TEAM:
                            return team1 == null ? flag : !team.isAlliedTo(team1) && flag;
                        default:
                            return true;
                    }
                }
            }

            return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && flag && !entity.isVehicle();
        }
    }

    //player code


    public ResourceLocation getTextureLocation(AbstractClientPlayerEntity entity) {
        return entity.getSkinTextureLocation();
    }


    public void renderRightArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, AbstractClientPlayerEntity playerIn) {
        this.renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (this.entityModel).rightArm, (this.entityModel).rightSleeve);
    }

    public void renderLeftArm(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, AbstractClientPlayerEntity playerIn) {
        this.renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (this.entityModel).leftArm, (this.entityModel).leftSleeve);
    }

    private void renderItem(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, AbstractClientPlayerEntity playerIn, ModelRenderer rendererArmIn, ModelRenderer rendererArmwearIn) {
        PlayerModel<AbstractClientPlayerEntity> playermodel = this.getModel();
        playermodel.attackTime = 0.0F;
        playermodel.crouching = false;
        playermodel.swimAmount = 0.0F;
        playermodel.setupAnim(playerIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rendererArmIn.xRot = 0.0F;
        rendererArmIn.render(matrixStackIn, bufferIn.getBuffer(RenderType.entitySolid(playerIn.getSkinTextureLocation())), combinedLightIn, OverlayTexture.NO_OVERLAY);
        rendererArmwearIn.xRot = 0.0F;
        rendererArmwearIn.render(matrixStackIn, bufferIn.getBuffer(RenderType.entityTranslucent(playerIn.getSkinTextureLocation())), combinedLightIn, OverlayTexture.NO_OVERLAY);
    }

    protected void applyRotations(AbstractClientPlayerEntity entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
        String s = TextFormatting.stripFormatting(entityLiving.getName().getString());
        if (("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(entityLiving instanceof PlayerEntity) || ((PlayerEntity)entityLiving).isModelPartShown(PlayerModelPart.CAPE))) {
            matrixStackIn.translate(0.0D, (double)(entityLiving.getBbHeight() + 0.1F), 0.0D);
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        }
    }

}
