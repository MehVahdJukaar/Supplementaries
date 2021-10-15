package net.mehvahdjukaar.supplementaries.client.renderers.entities;


import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.scores.Team;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;

import java.util.List;

public abstract class StatueEntityRenderer extends EntityRenderer<AbstractClientPlayer> implements RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    protected PlayerModel<AbstractClientPlayer> entityModel;
    protected final List<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> layerRenderers = Lists.newArrayList();

    public StatueEntityRenderer(EntityRenderDispatcher rendererManager, boolean useSmallArms) {
        super(rendererManager);
        this.entityModel = new PlayerModel<>(0.0F, useSmallArms);
        this.layerRenderers.add(new ItemInHandLayer<>(this));
        this.layerRenderers.add(new Deadmau5EarsLayer(this));
        this.layerRenderers.add(new CapeLayer(this));
        this.layerRenderers.add(new CustomHeadLayer<>(this));
    }

    public PlayerModel<AbstractClientPlayer> getModel() {
        return this.entityModel;
    }

    public void render(AbstractClientPlayer entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();

        float f = Mth.rotLerp(partialTicks, entityIn.yBodyRotO, entityIn.yBodyRot);
        float f1 = Mth.rotLerp(partialTicks, entityIn.yHeadRotO, entityIn.yHeadRot);
        float f2 = f1 - f;

        float f6 = Mth.lerp(partialTicks, entityIn.xRotO, entityIn.xRot);

        float f7 = this.handleRotationFloat(entityIn, partialTicks);
        this.applyRotations(entityIn, matrixStackIn, f7, f, partialTicks);
        matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
        //player scale
        matrixStackIn.scale(0.9375F, 0.9375F, 0.9375F);
        matrixStackIn.translate(0.0D, -1.501F, 0.0D);
        float f8 = 0.0F;
        float f5 = 0.0F;
        if (entityIn.isAlive()) {
            f8 = Mth.lerp(partialTicks, entityIn.animationSpeedOld, entityIn.animationSpeed);
            f5 = entityIn.animationPosition - entityIn.animationSpeed * (1.0F - partialTicks);
            if (f8 > 1.0F) {
                f8 = 1.0F;
            }
        }

        this.entityModel.prepareMobModel(entityIn, f5, f8, partialTicks);
        this.entityModel.setupAnim(entityIn, f5, f8, f7, f2, f6);

        RenderType rendertype = this.entityModel.renderType(this.getTextureLocation(entityIn));
        if (rendertype != null) {
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(rendertype);
            this.entityModel.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, 0, 1.0F, 1.0F, 1.0F, 1.0F);
        }


        for(RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> layerrenderer : this.layerRenderers) {
            layerrenderer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, f5, f8, partialTicks, f7, f2, f6);
        }


        matrixStackIn.popPose();
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }


    /**
     * Defines what float the third param in setRotationAngles of ModelBase is
     */
    protected float handleRotationFloat(AbstractClientPlayer livingBase, float partialTicks) {
        return (float)livingBase.tickCount + partialTicks;
    }

    protected boolean shouldShowName(AbstractClientPlayer entity) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        float f = entity.isDiscrete() ? 32.0F : 64.0F;
        if (d0 >= (double)(f * f)) {
            return false;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer clientplayerentity = minecraft.player;
            boolean flag = !entity.isInvisibleTo(clientplayerentity);
            if (entity != clientplayerentity) {
                Team team = entity.getTeam();
                Team team1 = clientplayerentity.getTeam();
                if (team != null) {
                    Team.Visibility team$visible = team.getNameTagVisibility();
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


    public ResourceLocation getTextureLocation(AbstractClientPlayer entity) {
        return entity.getSkinTextureLocation();
    }


    public void renderRightArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, AbstractClientPlayer playerIn) {
        this.renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (this.entityModel).rightArm, (this.entityModel).rightSleeve);
    }

    public void renderLeftArm(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, AbstractClientPlayer playerIn) {
        this.renderItem(matrixStackIn, bufferIn, combinedLightIn, playerIn, (this.entityModel).leftArm, (this.entityModel).leftSleeve);
    }

    private void renderItem(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, AbstractClientPlayer playerIn, ModelPart rendererArmIn, ModelPart rendererArmwearIn) {
        PlayerModel<AbstractClientPlayer> playermodel = this.getModel();
        playermodel.attackTime = 0.0F;
        playermodel.crouching = false;
        playermodel.swimAmount = 0.0F;
        playermodel.setupAnim(playerIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rendererArmIn.xRot = 0.0F;
        rendererArmIn.render(matrixStackIn, bufferIn.getBuffer(RenderType.entitySolid(playerIn.getSkinTextureLocation())), combinedLightIn, OverlayTexture.NO_OVERLAY);
        rendererArmwearIn.xRot = 0.0F;
        rendererArmwearIn.render(matrixStackIn, bufferIn.getBuffer(RenderType.entityTranslucent(playerIn.getSkinTextureLocation())), combinedLightIn, OverlayTexture.NO_OVERLAY);
    }

    protected void applyRotations(AbstractClientPlayer entityLiving, PoseStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
        String s = ChatFormatting.stripFormatting(entityLiving.getName().getString());
        if (("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(entityLiving instanceof Player) || ((Player)entityLiving).isModelPartShown(PlayerModelPart.CAPE))) {
            matrixStackIn.translate(0.0D, (double)(entityLiving.getBbHeight() + 0.1F), 0.0D);
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        }
    }

}
