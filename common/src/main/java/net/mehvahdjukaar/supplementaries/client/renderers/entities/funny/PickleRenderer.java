package net.mehvahdjukaar.supplementaries.client.renderers.entities.funny;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class PickleRenderer extends LivingEntityRenderer<AbstractClientPlayer, PickleModel<AbstractClientPlayer>> {

    public static PickleRenderer INSTANCE = null;

    public PickleRenderer(EntityRendererProvider.Context context) {
        super(context, new PickleModel<>(context.bakeLayer(ClientRegistry.PICKLE_MODEL)), 0.0125F);

        this.shadowStrength = 0;
        this.shadowRadius = 0;
        this.addLayer(new PlayerItemInHandLayer<>(this, context.getItemInHandRenderer()));

        this.addLayer(new PickleModel.PickleArmor<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));

        this.addLayer(new ArrowLayer<>(context, this));
        this.addLayer(new PickleModel.PickleElytra<>(this, context.getModelSet()));
        this.addLayer(new BeeStingerLayer<>(this));
    }

    protected float axisFacing = 0;
    protected boolean wasCrouching = false;

    @Override
    public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
        return ModTextures.SEA_PICKLE_RICK;
    }

    @Override
    protected boolean shouldShowName(AbstractClientPlayer player) {
        return !player.isCrouching() && super.shouldShowName(player);
    }

    @Override
    protected void scale(AbstractClientPlayer player, PoseStack stack, float partialTickTime) {
        stack.scale(0.5f, 0.5f, 0.5f);
    }

    @Override
    public void render(AbstractClientPlayer player, float p_225623_2_, float partialTicks, PoseStack matrixStack, MultiBufferSource p_225623_5_, int p_225623_6_) {
        this.model.partialTicks = partialTicks;
        this.setModelProperties(player);

        if (this.wasCrouching) {
            float f = (Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot) + axisFacing) % 360;
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(f));
        }
        super.render(player, p_225623_2_, partialTicks, matrixStack, p_225623_5_, p_225623_6_);
    }


    @Override
    public Vec3 getRenderOffset(AbstractClientPlayer player, float p_225627_2_) {
        return new Vec3(0.0D, -0.25D, 0.0D);
    }

    private void setModelProperties(AbstractClientPlayer player) {
        PlayerModel<AbstractClientPlayer> playermodel = this.getModel();
        playermodel.setAllVisible(false);
        boolean c = player.isCrouching();
        playermodel.body.visible = true;
        playermodel.leftArm.visible = !c;
        playermodel.rightArm.visible = !c;
        playermodel.leftLeg.visible = !c;
        playermodel.rightLeg.visible = !c;
        playermodel.head.visible = !c;
        playermodel.hat.visible = !c;

        if (this.wasCrouching != c && c) this.axisFacing = -player.getDirection().toYRot();
        this.wasCrouching = c;

        //playermodel.crouching = player.isCrouching();

        HumanoidModel.ArmPose poseRightArm = getArmPose(player, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose poseLeftArm = getArmPose(player, InteractionHand.OFF_HAND);

        if (poseRightArm.isTwoHanded()) {
            poseLeftArm = player.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }

        if (player.getMainArm() == HumanoidArm.RIGHT) {
            playermodel.rightArmPose = poseRightArm;
            playermodel.leftArmPose = poseLeftArm;
        } else {
            playermodel.rightArmPose = poseLeftArm;
            playermodel.leftArmPose = poseRightArm;
        }
    }

    protected static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0) {
                UseAnim useAnimation = itemstack.getUseAnimation();
                if (useAnimation == UseAnim.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (useAnimation == UseAnim.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (useAnimation == UseAnim.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }

                if (useAnimation == UseAnim.CROSSBOW && hand == player.getUsedItemHand()) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (useAnimation == UseAnim.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }
            } else if (!player.swinging && itemstack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            return HumanoidModel.ArmPose.ITEM;
        }
    }

    @Override
    protected void renderNameTag(AbstractClientPlayer player, Component name, PoseStack matrixStack, MultiBufferSource buffer, int p_225629_5_) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(player);
        matrixStack.pushPose();
        if (d0 < 100.0D) {
            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(2);
            if (objective != null) {
                Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), objective);
                super.renderNameTag(player, (Component.literal(Integer.toString(score.getScore()))).append(" ").append(objective.getDisplayName()), matrixStack, buffer, p_225629_5_);
                matrixStack.translate(0.0D, 9.0F * 1.15F * 0.025F, 0.0D);
            }
        }

        super.renderNameTag(player, name, matrixStack, buffer, p_225629_5_);
        matrixStack.popPose();
    }

    //same as vanilla
    @Override
    protected void setupRotations(AbstractClientPlayer player, PoseStack matrixStack, float p_225621_3_, float p_225621_4_, float partialTicks) {
        float f = player.getSwimAmount(partialTicks);
        if (player.isFallFlying()) {
            super.setupRotations(player, matrixStack, p_225621_3_, p_225621_4_, partialTicks);
            float f1 = (float) player.getFallFlyingTicks() + partialTicks;
            float inclination = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!player.isAutoSpinAttack()) {
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(inclination * (-90.0F - player.getXRot())));
            }

            Vec3 vector3d = player.getViewVector(partialTicks);
            Vec3 vector3d1 = player.getDeltaMovement();
            double d0 = vector3d1.horizontalDistanceSqr();
            double d1 = vector3d.horizontalDistanceSqr();
            if (d0 > 0.0D && d1 > 0.0D) {
                double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
                double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
                matrixStack.mulPose(Vector3f.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
            }
        } else if (f > 0.0F) {
            super.setupRotations(player, matrixStack, p_225621_3_, p_225621_4_, partialTicks);
            float f3 = player.isInWater() ? -90.0F - player.getXRot() : -90.0F;
            float f4 = Mth.lerp(f, 0.0F, f3);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(f4));
            if (player.isVisuallySwimming()) {
                matrixStack.translate(0.0D, -0.25, 0.25);
            }
        } else {
            super.setupRotations(player, matrixStack, p_225621_3_, p_225621_4_, partialTicks);
        }
    }

}