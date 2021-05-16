package net.mehvahdjukaar.supplementaries.client.renderers.entities;


import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.mehvahdjukaar.supplementaries.network.NetworkHandler;
import net.mehvahdjukaar.supplementaries.network.PicklePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Supplementaries.MOD_ID, value = {Dist.CLIENT})
public class PicklePlayer {
    private static PickleRenderer RENDERER_INSTANCE;
    private static JarredRenderer RENDERER_INSTANCE_JAR;
    private static boolean jarvis = false;
    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggedOutEvent event) {
        PickleData.onPlayerLogOff();
    }

    @SubscribeEvent
    public static void chat(ClientChatEvent event) {

        String m = event.getOriginalMessage();
        UUID id = Minecraft.getInstance().player.getGameProfile().getId();
        if(m.startsWith("/jarvis")){
            jarvis = !jarvis;
            event.setCanceled(true);
            if(jarvis)
            Minecraft.getInstance().player.sendMessage(
                    new StringTextComponent("I am Jarman"), Util.NIL_UUID);
        }

        else if (PickleData.isDev(id)) {
            if (m.startsWith("/pickle")) {

                event.setCanceled(true);
                boolean turnOn = !PickleData.isActive(id);

                if(turnOn) {
                    Minecraft.getInstance().player.sendMessage(
                            new StringTextComponent("I turned myself into a pickle!"), Util.NIL_UUID);
                }

                PickleData.set(id, turnOn);
                NetworkHandler.INSTANCE.sendToServer(new PicklePacket(id,turnOn));
            }
        }

    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        UUID id = event.getPlayer().getGameProfile().getId();

        if (PickleData.isActiveAndTick(id, event.getRenderer())) {
            event.setCanceled(true);
            if (RENDERER_INSTANCE == null) {
                RENDERER_INSTANCE = new PickleRenderer(event.getRenderer().getDispatcher());
            }

            float rot = MathHelper.rotLerp(event.getPlayer().yRotO, event.getPlayer().yRot, event.getPartialRenderTick());
            RENDERER_INSTANCE.render((AbstractClientPlayerEntity) event.getPlayer(), rot, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
        }
        else if(jarvis&&id.equals(Minecraft.getInstance().player.getUUID())){
            event.setCanceled(true);
            if (RENDERER_INSTANCE_JAR == null) {
                RENDERER_INSTANCE_JAR = new JarredRenderer(event.getRenderer().getDispatcher());
            }

            float rot = MathHelper.rotLerp(event.getPlayer().yRotO, event.getPlayer().yRot, event.getPartialRenderTick());
            RENDERER_INSTANCE_JAR.render((AbstractClientPlayerEntity) event.getPlayer(), rot, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());

        }

    }



    public static class JarredRenderer extends LivingRenderer<AbstractClientPlayerEntity, JarredModel<AbstractClientPlayerEntity>> {
        public JarredRenderer(EntityRendererManager mgr) {
            super(mgr, new JarredModel<>(), 0.0125F);
            this.shadowStrength = 0;
            this.shadowRadius = 0;
            this.addLayer(new HeldItemLayer<>(this));

            this.addLayer(new PickleArmor<>(this, new BipedModel<>(1.0F)));

            this.addLayer(new ArrowLayer<>(this));
            this.addLayer(new PickleElytra<>(this));
            this.addLayer(new BeeStingerLayer<>(this));
        }

        private float axisFacing = 0;
        private boolean wasCrouching = false;

        @Override
        public ResourceLocation getTextureLocation(AbstractClientPlayerEntity player) {
            return Textures.JAR_MAN;
        }

        @Override
        protected boolean shouldShowName(AbstractClientPlayerEntity player) {
            return !player.isCrouching() && super.shouldShowName(player);
        }

        @Override
        protected void scale(AbstractClientPlayerEntity player, MatrixStack stack, float partialTickTime) {
            stack.scale(1f, 1f, 1f);
        }

        @Override
        public void render(AbstractClientPlayerEntity player, float p_225623_2_, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
            this.setModelProperties(player);

            if (this.wasCrouching) {
                float f = (MathHelper.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot) + axisFacing) % 360;
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(f));
                matrixStack.translate(0,-0.125,0);
            }
            super.render(player, p_225623_2_, partialTicks, matrixStack, p_225623_5_, p_225623_6_);
        }

        @Override
        public Vector3d getRenderOffset(AbstractClientPlayerEntity player, float p_225627_2_) {
            return player.isCrouching() ? new Vector3d(0.0D, -0.5D, 0.0D) : new Vector3d(0.0D, -0.25D, 0.0D);
        }


        private void setModelProperties(AbstractClientPlayerEntity player) {
            PlayerModel<AbstractClientPlayerEntity> playermodel = this.getModel();
            playermodel.setAllVisible(false);
            boolean c = player.isCrouching();
            playermodel.body.visible = true;
            playermodel.head.visible = true;
            //playermodel.leftArm.visible = !c;
            //playermodel.rightArm.visible = !c;
            playermodel.leftLeg.visible = !c;
            playermodel.rightLeg.visible = !c;



            if (this.wasCrouching != c && c) this.axisFacing = -player.getDirection().toYRot();
            this.wasCrouching = c;

            //playermodel.crouching = player.isCrouching();

            BipedModel.ArmPose poseRightArm = getArmPose(player, Hand.MAIN_HAND);
            BipedModel.ArmPose poseLeftArm = getArmPose(player, Hand.OFF_HAND);

            if (poseRightArm.isTwoHanded()) {
                poseLeftArm = player.getOffhandItem().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
            }

            if (player.getMainArm() == HandSide.RIGHT) {
                playermodel.rightArmPose = poseRightArm;
                playermodel.leftArmPose = poseLeftArm;
            } else {
                playermodel.rightArmPose = poseLeftArm;
                playermodel.leftArmPose = poseRightArm;
            }


        }


        private static BipedModel.ArmPose getArmPose(AbstractClientPlayerEntity player, Hand hand) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (itemstack.isEmpty()) {
                return BipedModel.ArmPose.EMPTY;
            } else {
                if (player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0) {
                    UseAction useaction = itemstack.getUseAnimation();
                    if (useaction == UseAction.BLOCK) {
                        return BipedModel.ArmPose.BLOCK;
                    }

                    if (useaction == UseAction.BOW) {
                        return BipedModel.ArmPose.BOW_AND_ARROW;
                    }

                    if (useaction == UseAction.SPEAR) {
                        return BipedModel.ArmPose.THROW_SPEAR;
                    }

                    if (useaction == UseAction.CROSSBOW && hand == player.getUsedItemHand()) {
                        return BipedModel.ArmPose.CROSSBOW_CHARGE;
                    }
                } else if (!player.swinging && itemstack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack)) {
                    return BipedModel.ArmPose.CROSSBOW_HOLD;
                }

                return BipedModel.ArmPose.ITEM;
            }
        }


        @Override
        protected void renderNameTag(AbstractClientPlayerEntity player, ITextComponent name, MatrixStack matrixStack, IRenderTypeBuffer buffer, int p_225629_5_) {
            double d0 = this.entityRenderDispatcher.distanceToSqr(player);
            matrixStack.pushPose();
            if (d0 < 100.0D) {
                Scoreboard scoreboard = player.getScoreboard();
                ScoreObjective scoreobjective = scoreboard.getDisplayObjective(2);
                if (scoreobjective != null) {
                    Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), scoreobjective);
                    super.renderNameTag(player, (new StringTextComponent(Integer.toString(score.getScore()))).append(" ").append(scoreobjective.getDisplayName()), matrixStack, buffer, p_225629_5_);
                    matrixStack.translate(0.0D, (double) (9.0F * 1.15F * 0.025F), 0.0D);
                }
            }

            super.renderNameTag(player, name, matrixStack, buffer, p_225629_5_);
            matrixStack.popPose();
        }

        public void renderRightHand(MatrixStack p_229144_1_, IRenderTypeBuffer p_229144_2_, int p_229144_3_, AbstractClientPlayerEntity p_229144_4_) {
            this.renderHand(p_229144_1_, p_229144_2_, p_229144_3_, p_229144_4_, (this.model).rightArm, (this.model).rightSleeve);
        }

        public void renderLeftHand(MatrixStack p_229146_1_, IRenderTypeBuffer p_229146_2_, int p_229146_3_, AbstractClientPlayerEntity p_229146_4_) {
            this.renderHand(p_229146_1_, p_229146_2_, p_229146_3_, p_229146_4_, (this.model).leftArm, (this.model).leftSleeve);
        }

        private void renderHand(MatrixStack matrixStack, IRenderTypeBuffer buffer, int p_229145_3_, AbstractClientPlayerEntity player, ModelRenderer p_229145_5_, ModelRenderer p_229145_6_) {
            PlayerModel<AbstractClientPlayerEntity> playermodel = this.getModel();
            this.setModelProperties(player);
            playermodel.attackTime = 0.0F;
            playermodel.crouching = false;
            playermodel.swimAmount = 0.0F;
            playermodel.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            p_229145_5_.xRot = 0.0F;
            p_229145_5_.render(matrixStack, buffer.getBuffer(RenderType.entitySolid(player.getSkinTextureLocation())), p_229145_3_, OverlayTexture.NO_OVERLAY);
            p_229145_6_.xRot = 0.0F;
            p_229145_6_.render(matrixStack, buffer.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation())), p_229145_3_, OverlayTexture.NO_OVERLAY);
        }

        @Override
        protected void setupRotations(AbstractClientPlayerEntity player, MatrixStack matrixStack, float p_225621_3_, float p_225621_4_, float p_225621_5_) {
            float f = player.getSwimAmount(p_225621_5_);
            if (player.isFallFlying()) {
                super.setupRotations(player, matrixStack, p_225621_3_, p_225621_4_, p_225621_5_);
                float f1 = (float) player.getFallFlyingTicks() + p_225621_5_;
                float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
                if (!player.isAutoSpinAttack()) {
                    matrixStack.mulPose(Vector3f.XP.rotationDegrees(f2 * (-90.0F - player.xRot)));
                }

                Vector3d vector3d = player.getViewVector(p_225621_5_);
                Vector3d vector3d1 = player.getDeltaMovement();
                double d0 = Entity.getHorizontalDistanceSqr(vector3d1);
                double d1 = Entity.getHorizontalDistanceSqr(vector3d);
                if (d0 > 0.0D && d1 > 0.0D) {
                    double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
                    double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
                    matrixStack.mulPose(Vector3f.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
                }
            } else if (f > 0.0F) {
                super.setupRotations(player, matrixStack, p_225621_3_, p_225621_4_, p_225621_5_);
                float f3 = player.isInWater() ? -90.0F - player.xRot : -90.0F;
                float f4 = MathHelper.lerp(f, 0.0F, f3);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(f4));
                if (player.isVisuallySwimming()) {
                    matrixStack.translate(0.0D, -0.25, 0.25);
                }
            } else {
                super.setupRotations(player, matrixStack, p_225621_3_, p_225621_4_, p_225621_5_);
            }


        }

    }




    public static class JarredModel<T extends LivingEntity> extends PlayerModel<T> {
        private final ModelRenderer eyeLeft;
        private final ModelRenderer eyeRight;
        public JarredModel() {
            super(0f, false);
            this.texWidth = 64;
            this.texHeight = 64;

            body = new ModelRenderer(this);
            body.setPos(0.0F, 0.0F, 0.0F);
            body.texOffs(0, 0).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 14.0F, 10.0F, 0.0F, false);
            body.texOffs(40, 0).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 3.0F, 6.0F, 0.0F, false);
            body.texOffs(0, 24).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 11.0F, 8.0F, 0.0F, false);

            leftLeg = new ModelRenderer(this);
            leftLeg.setPos(-1.9F, 12.0F, 0.0F);
            leftLeg.texOffs(51, 1).addBox(3.9F, 0.0F, -1.0F, 1.0F, 5.0F, 2.0F, 0.0F, true);

            rightLeg = new ModelRenderer(this);
            rightLeg.setPos(1.9F, 12.0F, 0.0F);
            rightLeg.texOffs(46, 1).addBox(-4.9F, 0.0F, -1.0F, 1.0F, 5.0F, 2.0F, 0.0F, false);


            head = new ModelRenderer(this);
            eyeRight = new ModelRenderer(this);
            eyeLeft = new ModelRenderer(this);
            head.setPos(0.0F, 5.0F, 0.0F);
            head.texOffs(42, 10).addBox(-3.0F, -2.999F, -1.5F, 6.0F, 3.0F, 3.0F, 0.0F, false);
            head.texOffs(45, 12).addBox(-2.0F, -2.999F, 1.5F, 4.0F, 3.0F, 1.0F, 0.0F, false);
            head.texOffs(40, 16).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 1.0F, 3.0F, 0.0F, false);
            head.texOffs(40, 20).addBox(-1.0F, 1.0F, 0.5F, 2.0F, 1.0F, 2.0F, 0.0F, false);
            eyeRight.texOffs(30, 6).addBox(1.0F, 0.0F, -2.499F, 2.0F, 2.0F, 2.0F, 0.0F, false);
            eyeLeft.texOffs(30, 6).addBox(-3.0F, -1.0F, -2.499F, 2.0F, 2.0F, 2.0F, 0.0F, false);
            head.addChild(eyeLeft);
            head.addChild(eyeRight);
        }

        @Override
        public void translateToHand(HandSide handSide, MatrixStack matrixStack) {
            matrixStack.translate(0, 0.8, -0.5);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
            matrixStack.scale(0.5f,0.5f,0.5f);
            ModelRenderer modelrenderer = this.getArm(handSide);

            float f = 1F * (float) (handSide == HandSide.RIGHT ? 1 : -1);
            modelrenderer.x += f;
            modelrenderer.y -= 1;
            modelrenderer.z += 1;
            //modelrenderer.translateAndRotate(matrixStack);
            modelrenderer.z -= 1;
        }

        @Override
        public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder p_225598_2_, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {

            matrixStack.pushPose();
            matrixStack.translate(0, this.riding ? -0.5 : 0.5f, 0);

            super.renderToBuffer(matrixStack, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            matrixStack.popPose();
        }


        @Override
        public void setupAnim(T player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            if (swimAmount > 0 && player.isVisuallySwimming()) {
                this.body.yRot = this.rotlerpRad(limbSwing, this.body.yRot, (-(float) Math.PI / 30F));
            }
            //this.head.copyFrom(this.body);

            head.y = 6f+MathHelper.sin(limbSwing/3f)/2f;

            eyeRight.x =  MathHelper.cos(ageInTicks/16f)/4f;
            eyeRight.y = MathHelper.sin(ageInTicks/7f)/4f;

            eyeLeft.x = MathHelper.cos(ageInTicks/12f)/4f;
            eyeLeft.y = MathHelper.cos(ageInTicks/7f)/4f;

            //float f = (MathHelper.rotLerp(limbSwingAmount, player.yBodyRotO, player.yBodyRot))%360;
            //this.body.yRot = -f / (180F / (float) Math.PI);
        }
    }



    public static class PickleRenderer extends LivingRenderer<AbstractClientPlayerEntity,PickleModel<AbstractClientPlayerEntity>> {
        public PickleRenderer(EntityRendererManager mgr) {
            super(mgr, new PickleModel<>(), 0.0125F);
            this.shadowStrength=0;
            this.shadowRadius=0;
            this.addLayer(new HeldItemLayer<>(this));

            this.addLayer(new PickleArmor<>(this, new BipedModel<>(1.0F)));

            this.addLayer(new ArrowLayer<>(this));
            this.addLayer(new PickleElytra<>(this));
            this.addLayer(new BeeStingerLayer<>(this));
        }

        private float axisFacing = 0;
        private boolean wasCrouching = false;

        @Override
        public ResourceLocation getTextureLocation(AbstractClientPlayerEntity player) {
            return Textures.SEA_PICKLE_RICK;
        }

        @Override
        protected boolean shouldShowName(AbstractClientPlayerEntity player) {
            return !player.isCrouching() && super.shouldShowName(player);
        }

        @Override
        protected void scale(AbstractClientPlayerEntity player, MatrixStack stack, float partialTickTime) {
            stack.scale(0.5f, 0.5f, 0.5f);
        }

        @Override
        public void render(AbstractClientPlayerEntity player, float p_225623_2_, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
            this.setModelProperties(player);

            if(this.wasCrouching) {
                float f = (MathHelper.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot) + axisFacing) % 360;
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(f));
            }
            super.render(player, p_225623_2_, partialTicks, matrixStack, p_225623_5_, p_225623_6_);
        }




        @Override
        public Vector3d getRenderOffset(AbstractClientPlayerEntity player, float p_225627_2_) {
            return player.isCrouching() ? new Vector3d(0.0D, -0.25D, 0.0D) :  new Vector3d(0.0D, -0.25D, 0.0D);
        }


        private void setModelProperties(AbstractClientPlayerEntity player) {
            PlayerModel<AbstractClientPlayerEntity> playermodel = this.getModel();
            playermodel.setAllVisible(false);
            boolean c = player.isCrouching();
            playermodel.body.visible = true;
            playermodel.leftArm.visible = !c;
            playermodel.rightArm.visible = !c;
            playermodel.leftLeg.visible = !c;
            playermodel.rightLeg.visible = !c;
            playermodel.head.visible = !c;
            playermodel.hat.visible = !c;

            if(this.wasCrouching != c &&c)this.axisFacing=-player.getDirection().toYRot();
            this.wasCrouching = c;

            //playermodel.crouching = player.isCrouching();

            BipedModel.ArmPose poseRightArm = getArmPose(player, Hand.MAIN_HAND);
            BipedModel.ArmPose poseLeftArm = getArmPose(player, Hand.OFF_HAND);

            if (poseRightArm.isTwoHanded()) {
                poseLeftArm = player.getOffhandItem().isEmpty() ? BipedModel.ArmPose.EMPTY : BipedModel.ArmPose.ITEM;
            }

            if (player.getMainArm() == HandSide.RIGHT) {
                playermodel.rightArmPose = poseRightArm;
                playermodel.leftArmPose = poseLeftArm;
            } else {
                playermodel.rightArmPose = poseLeftArm;
                playermodel.leftArmPose = poseRightArm;
            }
        }


        private static BipedModel.ArmPose getArmPose(AbstractClientPlayerEntity player, Hand hand) {
            ItemStack itemstack = player.getItemInHand(hand);
            if (itemstack.isEmpty()) {
                return BipedModel.ArmPose.EMPTY;
            } else {
                if (player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0) {
                    UseAction useaction = itemstack.getUseAnimation();
                    if (useaction == UseAction.BLOCK) {
                        return BipedModel.ArmPose.BLOCK;
                    }

                    if (useaction == UseAction.BOW) {
                        return BipedModel.ArmPose.BOW_AND_ARROW;
                    }

                    if (useaction == UseAction.SPEAR) {
                        return BipedModel.ArmPose.THROW_SPEAR;
                    }

                    if (useaction == UseAction.CROSSBOW && hand == player.getUsedItemHand()) {
                        return BipedModel.ArmPose.CROSSBOW_CHARGE;
                    }
                } else if (!player.swinging && itemstack.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(itemstack)) {
                    return BipedModel.ArmPose.CROSSBOW_HOLD;
                }

                return BipedModel.ArmPose.ITEM;
            }
        }


        @Override
        protected void renderNameTag(AbstractClientPlayerEntity player, ITextComponent name, MatrixStack matrixStack, IRenderTypeBuffer buffer, int p_225629_5_) {
            double d0 = this.entityRenderDispatcher.distanceToSqr(player);
            matrixStack.pushPose();
            if (d0 < 100.0D) {
                Scoreboard scoreboard = player.getScoreboard();
                ScoreObjective scoreobjective = scoreboard.getDisplayObjective(2);
                if (scoreobjective != null) {
                    Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), scoreobjective);
                    super.renderNameTag(player, (new StringTextComponent(Integer.toString(score.getScore()))).append(" ").append(scoreobjective.getDisplayName()), matrixStack, buffer, p_225629_5_);
                    matrixStack.translate(0.0D, (double)(9.0F * 1.15F * 0.025F), 0.0D);
                }
            }

            super.renderNameTag(player, name, matrixStack, buffer, p_225629_5_);
            matrixStack.popPose();
        }

        public void renderRightHand(MatrixStack p_229144_1_, IRenderTypeBuffer p_229144_2_, int p_229144_3_, AbstractClientPlayerEntity p_229144_4_) {
            this.renderHand(p_229144_1_, p_229144_2_, p_229144_3_, p_229144_4_, (this.model).rightArm, (this.model).rightSleeve);
        }

        public void renderLeftHand(MatrixStack p_229146_1_, IRenderTypeBuffer p_229146_2_, int p_229146_3_, AbstractClientPlayerEntity p_229146_4_) {
            this.renderHand(p_229146_1_, p_229146_2_, p_229146_3_, p_229146_4_, (this.model).leftArm, (this.model).leftSleeve);
        }

        private void renderHand(MatrixStack matrixStack, IRenderTypeBuffer buffer, int p_229145_3_, AbstractClientPlayerEntity player, ModelRenderer p_229145_5_, ModelRenderer p_229145_6_) {
            PlayerModel<AbstractClientPlayerEntity> playermodel = this.getModel();
            this.setModelProperties(player);
            playermodel.attackTime = 0.0F;
            playermodel.crouching = false;
            playermodel.swimAmount = 0.0F;
            playermodel.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
            p_229145_5_.xRot = 0.0F;
            p_229145_5_.render(matrixStack, buffer.getBuffer(RenderType.entitySolid(player.getSkinTextureLocation())), p_229145_3_, OverlayTexture.NO_OVERLAY);
            p_229145_6_.xRot = 0.0F;
            p_229145_6_.render(matrixStack, buffer.getBuffer(RenderType.entityTranslucent(player.getSkinTextureLocation())), p_229145_3_, OverlayTexture.NO_OVERLAY);
        }

        @Override
        protected void setupRotations(AbstractClientPlayerEntity player, MatrixStack matrixStack, float p_225621_3_, float p_225621_4_, float partialTicks) {
            float f = player.getSwimAmount(partialTicks);
            if (player.isFallFlying()) {
                super.setupRotations(player, matrixStack, p_225621_3_, p_225621_4_, partialTicks);
                float f1 = (float)player.getFallFlyingTicks() + partialTicks;
                float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
                if (!player.isAutoSpinAttack()) {
                    matrixStack.mulPose(Vector3f.XP.rotationDegrees(f2 * (-90.0F - player.xRot)));
                }

                Vector3d vector3d = player.getViewVector(partialTicks);
                Vector3d vector3d1 = player.getDeltaMovement();
                double d0 = Entity.getHorizontalDistanceSqr(vector3d1);
                double d1 = Entity.getHorizontalDistanceSqr(vector3d);
                if (d0 > 0.0D && d1 > 0.0D) {
                    double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
                    double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
                    matrixStack.mulPose(Vector3f.YP.rotation((float)(Math.signum(d3) * Math.acos(d2))));
                }
            }
            else if (f > 0.0F) {
                super.setupRotations(player, matrixStack, p_225621_3_, p_225621_4_, partialTicks);
                float f3 = player.isInWater() ? -90.0F - player.xRot : -90.0F;
                float f4 = MathHelper.lerp(f, 0.0F, f3);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(f4));
                if (player.isVisuallySwimming()) {
                    matrixStack.translate(0.0D, -0.25, 0.25F);
                }
            } else {
                super.setupRotations(player, matrixStack, p_225621_3_, p_225621_4_, partialTicks);
            }


        }

    }



    public static class PickleModel<T extends LivingEntity> extends PlayerModel<T> {
        public PickleModel() {
            super(0f,false);
            this.texWidth = 32;
            this.texHeight = 32;

            float o = 0;

            head = new ModelRenderer(this);
            head.setPos(0,13,0);
            hat = new ModelRenderer(this);
            hat.copyFrom(head);

            body = new ModelRenderer(this);
            body.setPos(0.0F, 0.0F+o, 0.0F);
            body.texOffs(0, 2).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 14.0F, 8.0F, 0.0F, false);

            leftArm = new ModelRenderer(this);
            leftArm.setPos(5.0F, 2.5F+o, 0.0F);
            leftArm.texOffs(2, 18).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 8.0F, 2.0F, 0.0F, false);

            rightArm = new ModelRenderer(this);
            rightArm.setPos(-5.0F, 2.5F+o, 0.0F);
            rightArm.texOffs(16, 18).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 8.0F, 2.0F, 0.0F, false);

            leftLeg = new ModelRenderer(this);
            leftLeg.setPos(-1.9F, 12.0F+o, 0.0F);
            leftLeg.texOffs(0, 24).addBox(3.85F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, true);

            rightLeg = new ModelRenderer(this);
            rightLeg.setPos(1.9F, 12.0F+o, 0.0F);
            rightLeg.texOffs(16, 24).addBox(-5.85F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, 0.0F, false);
        }

        @Override
        public void translateToHand(HandSide handSide, MatrixStack matrixStack) {
            matrixStack.translate(0,0.5,0);
            ModelRenderer modelrenderer = this.getArm(handSide);

            float f = 1F * (float)(handSide == HandSide.RIGHT ? 1 : -1);
            modelrenderer.x += f;
            modelrenderer.y -= 1;
            modelrenderer.z += 1;
            modelrenderer.translateAndRotate(matrixStack);
            modelrenderer.z -= 1;
        }

        @Override
        public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder p_225598_2_, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {

            matrixStack.pushPose();
            matrixStack.translate(0, this.riding?-0.5:0.5f, 0);

            super.renderToBuffer(matrixStack, p_225598_2_, p_225598_3_, p_225598_4_, p_225598_5_, p_225598_6_, p_225598_7_, p_225598_8_);
            matrixStack.popPose();
        }

        @Override
        public void setupAnim(T player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
            super.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            if (swimAmount > 0 && player.isVisuallySwimming()) {
                this.body.yRot = this.rotlerpRad(limbSwing, this.body.yRot, (-(float) Math.PI / 30F));
            }
            //float f = (MathHelper.rotLerp(limbSwingAmount, player.yBodyRotO, player.yBodyRot))%360;
            //this.body.yRot = -f / (180F / (float) Math.PI);
        }
    }


    public static class PickleArmor<T extends LivingEntity, M extends BipedModel<T>, A extends BipedModel<T>> extends BipedArmorLayer<T, M, A> {

        public PickleArmor(IEntityRenderer<T, M> renderer, A modelChest) {
            super(renderer, modelChest, modelChest);
        }

        @Override
        public void setPartVisibility(A modelIn, EquipmentSlotType slotIn) {
            modelIn.setAllVisible(false);
            boolean head = slotIn == EquipmentSlotType.HEAD;
            modelIn.hat.visible = head;
            modelIn.head.visible = head;
            modelIn.head.copyFrom(modelIn.body);
            modelIn.head.y=13;
            modelIn.hat.copyFrom(modelIn.head);
        }

        @Override
        public void render(MatrixStack p_225628_1_, IRenderTypeBuffer p_225628_2_, int p_225628_3_, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
            if(entity.isCrouching())return;
            super.render(p_225628_1_, p_225628_2_, p_225628_3_, entity, p_225628_5_, p_225628_6_, p_225628_7_, p_225628_8_, p_225628_9_, p_225628_10_);
        }
    }

    public static class PickleElytra<T extends LivingEntity, M extends EntityModel<T>> extends ElytraLayer<T, M> {

        public PickleElytra(IEntityRenderer<T, M> renderer) {
            super(renderer);
        }

        @Override
        public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int p_225628_3_, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
            matrixStack.translate(0,0.625,0.09375);
            matrixStack.scale(0.625f,0.625f,0.625f);

            super.render(matrixStack,buffer,p_225628_3_,entity,p_225628_5_,p_225628_6_,p_225628_7_,p_225628_8_,p_225628_9_,p_225628_10_);
        }

        @Override
        public boolean shouldRender(ItemStack stack, T entity) {
            if(entity.isCrouching())return false;
            return super.shouldRender(stack, entity);
        }
    }


    //server and client side. might move into data
    public static class PickleData {
        private static final ImmutableList<UUID> DEVS = ImmutableList.of(
                UUID.fromString("380df991-f603-344c-a090-369bad2a924a"),
                UUID.fromString("898b3a39-e486-405c-a873-d6b472dc3ba2"),

                UUID.fromString("720f165c-b066-4113-9622-63fc63c65696"),
                UUID.fromString("5672a110-0d77-3aaa-b35c-3143bed23e33"),
                UUID.fromString("df3aa2e6-b821-4c7d-8833-88be79a9079b"),
                UUID.nameUUIDFromBytes(("OfflinePlayer:" + "TheEvilGolem").getBytes(Charsets.UTF_8)));

        public static final Map<UUID, PickleValues> PICKLE_PLAYERS = new HashMap<>();

        static{


            for(UUID id : DEVS)PICKLE_PLAYERS.put(id,new PickleValues());
        }

        //reset
        public static void onPlayerLogOff(){
            for(PickleValues val : PICKLE_PLAYERS.values()){
                val.reset();
            }
        }

        public static void onPlayerLogin(PlayerEntity player){
            for(UUID id : PICKLE_PLAYERS.keySet()){
                boolean on = PICKLE_PLAYERS.get(id).isOn();
                if(on){
                    //to client
                    NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                            new PicklePacket(id,on));
                }
            }
        }

        public static boolean isDev(UUID id) {
            return DEVS.contains(id);
        }

        public static void set(UUID id, boolean on){
            PICKLE_PLAYERS.getOrDefault(id,DEF).toggle(on);
        }

        public static boolean isActiveAndTick(UUID id, PlayerRenderer renderer){
            return PICKLE_PLAYERS.getOrDefault(id,DEF).isOnAndTick(renderer);
        }
        public static boolean isActive(UUID id){
            return PICKLE_PLAYERS.getOrDefault(id,DEF).isOn();
        }

        private static final PickleValues DEF = new PickleValues();

        public static class PickleValues{
            private State state = State.OFF;
            private float oldShadowSize = 1;

            public void toggle(boolean on){
                if(on) this.state = State.FIRST_ON;
                else this.state = State.FIRST_OFF;
            }
            public void reset(){this.state = State.OFF;}

            public boolean isOnAndTick(PlayerRenderer renderer){
                switch (this.state){
                    case ON:
                        return true;
                    default:
                    case OFF:
                        return false;
                    case FIRST_ON:
                        this.oldShadowSize = renderer.shadowRadius;
                        renderer.shadowRadius = 0;
                        this.state = State.ON;
                        return true;
                    case FIRST_OFF:
                        renderer.shadowRadius = this.oldShadowSize;
                        this.state = State.OFF;
                        return true;
                }
            }
            public boolean isOn(){
                return this.state==State.ON||this.state==State.FIRST_ON;
            }

            private enum State{
                ON,OFF,FIRST_ON,FIRST_OFF;
            }
        }
    }


}
