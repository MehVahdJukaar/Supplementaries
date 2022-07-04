package net.mehvahdjukaar.supplementaries.client.block_models;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.common.block.tiles.StatueBlockTile;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
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
        super(RenderType::entityTranslucent);
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


    public void renderEars(PoseStack p_228287_1_, VertexConsumer p_228287_2_, int p_228287_3_, int p_228287_4_) {
        this.ear.copyFrom(this.head);
        this.ear.x = 0.0F;
        this.ear.y = 0.0F;
        this.ear.render(p_228287_1_, p_228287_2_, p_228287_3_, p_228287_4_);
    }

    public void renderCloak(PoseStack p_228289_1_, VertexConsumer p_228289_2_, int p_228289_3_, int p_228289_4_) {
        this.cloak.render(p_228289_1_, p_228289_2_, p_228289_3_, p_228289_4_);
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

    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat,
                this.leftPants, this.rightPants, this.leftSleeve, this.rightSleeve, this.jacket);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
                               float red, float green, float blue, float alpha) {

        head.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        hat.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        jacket.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        rightLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        leftLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        leftPants.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        rightPants.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);


        rightArmS.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        leftArmS.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        leftSleeveS.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        rightSleeveS.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

        rightArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        leftArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        leftSleeve.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        rightSleeve.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);




    }
}

