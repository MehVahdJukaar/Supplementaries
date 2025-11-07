package net.mehvahdjukaar.supplementaries.client.renderers.entities.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.StatueBlockTile;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class StatueEntityModel extends Model {
    //biped
    public final ModelPart head;
    public final ModelPart hat;
    public final ModelPart body;
    public final ModelPart rightArm;
    public final ModelPart leftArm;
    public final ModelPart rightLeg;
    public final ModelPart leftLeg;
    //player
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final ModelPart cloak;
    private final ModelPart ear;

    //slim
    public final ModelPart rightArmS;
    public final ModelPart leftArmS;
    public final ModelPart leftSleeveS;
    public final ModelPart rightSleeveS;

    public StatueEntityModel(BlockEntityRendererProvider.Context context) {
        super(RenderType::entityTranslucentCull);
        ModelPart modelPart = context.bakeLayer(ModelLayers.PLAYER);
        this.head = modelPart.getChild("head");
        this.hat = modelPart.getChild("hat");
        this.body = modelPart.getChild("body");
        this.rightArm = modelPart.getChild("right_arm");
        this.leftArm = modelPart.getChild("left_arm");
        this.rightLeg = modelPart.getChild("right_leg");
        this.leftLeg = modelPart.getChild("left_leg");
        this.ear = modelPart.getChild("ear");
        this.cloak = modelPart.getChild("cloak");
        this.leftSleeve = modelPart.getChild("left_sleeve");
        this.rightSleeve = modelPart.getChild("right_sleeve");
        this.leftPants = modelPart.getChild("left_pants");
        this.rightPants = modelPart.getChild("right_pants");
        this.jacket = modelPart.getChild("jacket");

        ModelPart modelPartSlim = context.bakeLayer(ModelLayers.PLAYER_SLIM);
        this.rightArmS = modelPartSlim.getChild("right_arm");
        this.leftArmS = modelPartSlim.getChild("left_arm");
        this.leftSleeveS = modelPartSlim.getChild("left_sleeve");
        this.rightSleeveS = modelPartSlim.getChild("right_sleeve");
    }


    public void renderEars(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        this.ear.copyFrom(this.head);
        this.ear.x = 0.0F;
        this.ear.y = 0.0F;
        this.ear.render(poseStack, consumer, light, overlay);
    }

    public void renderCloak(PoseStack poseStack, VertexConsumer consumer, int light, int overlay, StatueBlockTile tile, float partialTick) {
        BlockPos pos=tile.getBlockPos();
        long gameTime = tile.getLevel() == null ? 0 : tile.getLevel().getGameTime();
        float time = ((float)Math.floorMod((pos.getX() * 7L + pos.getY() * 9L + pos.getZ() * 13L) + gameTime, 100L) + partialTick) / 100.0F;
        this.cloak.xRot = (-0.04F + 0.01F * Mth.cos((float) (Math.PI * 2) * time)) * (float) Math.PI;
       this.cloak.yRot = Mth.PI;
        this.cloak.z = 2;
        this.cloak.render(poseStack, consumer, light, overlay);
    }


    public void setupAnim(long ticks, float partialTricks, Direction dir, StatueBlockTile.StatuePose pose, boolean waving, boolean slim) {

        rightArmS.visible = slim;
        leftArmS.visible = slim;
        leftSleeveS.visible = slim;
        rightSleeveS.visible = slim;

        rightArm.visible = !slim;
        leftArm.visible = !slim;
        leftSleeve.visible = !slim;
        rightSleeve.visible = !slim;


        int d = dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? -1 : 1;

        switch (pose) {
            case SWORD -> {
                this.leftLeg.xRot = 0f;
                this.rightLeg.xRot = 0f;
                this.leftArm.xRot = (float) (Math.PI / 10 * -4);
                this.leftArm.yRot = (float) (Math.PI / 10 * 1.5);
                this.rightArm.xRot = (float) (Math.PI / 10 * -4);
                this.rightArm.yRot = (float) (-Math.PI / 10 * 1.5);
            }
            case TOOL -> {
                this.leftLeg.xRot = 0f;
                this.rightLeg.xRot = 0f;
                this.leftArm.xRot = (float) (Math.PI / 10 * -3);
                this.leftArm.yRot = (float) (Math.PI / 10 * 1.5);
                this.rightArm.xRot = (float) (Math.PI / 10 * -3);
                this.rightArm.yRot = (float) (-Math.PI / 10 * 1.5);
            }
            case HOLDING, GLOBE, SEPIA_GLOBE -> {
                this.leftLeg.xRot = 0f;
                this.rightLeg.xRot = 0f;
                this.leftArm.xRot = (float) -(Math.PI / 4f);
                this.rightArm.xRot = (float) -(Math.PI / 4f);
                this.leftArm.yRot = 0;
                this.rightArm.yRot = 0;
            }
            case STANDING -> {
                this.leftLeg.xRot = (float) (Math.PI / 8f) * d;
                this.rightLeg.xRot = (float) (-Math.PI / 8f) * d;
                this.leftArm.xRot = (float) (-Math.PI / 8f) * d;
                this.rightArm.xRot = (float) (Math.PI / 8f) * d;
                this.leftArm.yRot = 0;
                this.rightArm.yRot = 0;
            }
            case CANDLE -> {
                this.leftLeg.xRot = 0f;
                this.rightLeg.xRot = 0f;
                this.leftArm.xRot = (float) -(Math.PI / 8f);
                this.rightArm.xRot = (float) -(Math.PI / 8f);
                this.leftArm.yRot = 0;
                this.rightArm.yRot = 0;
            }
        }


        if (waving) {
            this.rightArm.yRot = 0;
            this.rightArm.xRot = (float) (Math.PI);
            float f2 = ((float) Math.floorMod(ticks, 15L) + partialTricks) / 15.0F;
            this.rightArm.zRot = -0.5f - 0.5f * Mth.sin(((float) Math.PI * 2F) * f2);
        } else {
            this.rightArm.zRot = 0;
        }


        this.hat.copyFrom(this.head);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
        this.leftArmS.copyFrom(this.leftArm);
        this.rightArmS.copyFrom(this.rightArm);
        this.leftSleeveS.copyFrom(this.leftArm);
        this.rightSleeveS.copyFrom(this.rightArm);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
                               int color) {

        head.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        hat.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        jacket.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        rightLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        leftLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        leftPants.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        rightPants.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);


        rightArmS.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        leftArmS.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        leftSleeveS.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        rightSleeveS.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);

        rightArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        leftArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        leftSleeve.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
        rightSleeve.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
    }
}

