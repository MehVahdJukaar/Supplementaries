package net.mehvahdjukaar.supplementaries.client.models;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.block.tiles.StatueBlockTile;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class StatueEntityModel extends Model {
    //biped
    public ModelPart head;
    public ModelPart hat;
    public ModelPart body;
    public ModelPart rightArm;
    public ModelPart leftArm;
    public ModelPart rightLeg;
    public ModelPart leftLeg;
    //player
    public final ModelPart leftSleeve;
    public final ModelPart rightSleeve;
    public final ModelPart leftPants;
    public final ModelPart rightPants;
    public final ModelPart jacket;
    private final ModelPart cloak;
    private final ModelPart ear;

    //slim
    public ModelPart rightArmS;
    public ModelPart leftArmS;
    public final ModelPart leftSleeveS;
    public final ModelPart rightSleeveS;

    public StatueEntityModel(float offset) {

        super(RenderType::entityTranslucent);

        //biped
        this.texWidth = 64;
        this.texHeight = 64;
        this.head = new ModelPart(this, 0, 0);
        this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, offset);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.hat = new ModelPart(this, 32, 0);
        this.hat.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, offset + 0.5F);
        this.hat.setPos(0.0F, 0.0F, 0.0F);
        this.body = new ModelPart(this, 16, 16);
        this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, offset);
        this.body.setPos(0.0F, 0.0F, 0.0F);
        //player
        this.ear = new ModelPart(this, 24, 0);
        this.ear.addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, offset);
        this.cloak = new ModelPart(this, 0, 0);
        this.cloak.setTexSize(64, 32);
        this.cloak.addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, offset);

        this.leftArmS = new ModelPart(this, 32, 48);
        this.leftArmS.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, offset);
        this.leftArmS.setPos(6.0F, 2.5F, 0.0F);
        this.rightArmS = new ModelPart(this, 40, 16);
        this.rightArmS.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, offset);
        this.rightArmS.setPos(-6.0F, 2.5F, 0.0F);
        this.leftSleeveS = new ModelPart(this, 48, 48);
        this.leftSleeveS.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, offset + 0.25F);
        this.leftSleeveS.setPos(6.0F, 2.5F, 0.0F);
        this.rightSleeveS = new ModelPart(this, 40, 32);
        this.rightSleeveS.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, offset + 0.25F);
        this.rightSleeveS.setPos(-6.0F, 2.5F, 10.0F);


        this.leftArm = new ModelPart(this, 32, 48);
        this.leftArm.addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, offset);
        this.leftArm.setPos(6.0F, 2.0F, 0.0F);
        this.leftSleeve = new ModelPart(this, 48, 48);
        this.leftSleeve.addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, offset + 0.25F);
        this.leftSleeve.setPos(6.0F, 2.0F, 0.0F);
        this.rightSleeve = new ModelPart(this, 40, 32);
        this.rightSleeve.addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, offset + 0.25F);
        this.rightSleeve.setPos(-6.0F, 2.0F, 10.0F);
        this.rightArm = new ModelPart(this, 40, 16);
        this.rightArm.addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, offset);
        this.rightArm.setPos(-6.0F, 2.0F, 0.0F);

        this.rightLeg = new ModelPart(this, 0, 16);
        this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, offset);
        this.rightLeg.setPos(-1.9F, 12.0F , 0.0F);
        this.leftLeg = new ModelPart(this, 16, 48);
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, offset);
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.leftPants = new ModelPart(this, 0, 48);
        this.leftPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, offset + 0.25F);
        this.leftPants.setPos(1.9F, 12.0F, 0.0F);
        this.rightPants = new ModelPart(this, 0, 32);
        this.rightPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, offset + 0.25F);
        this.rightPants.setPos(-1.9F, 12.0F, 0.0F);
        this.jacket = new ModelPart(this, 16, 32);
        this.jacket.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, offset + 0.25F);
        this.jacket.setPos(0.0F, 0.0F, 0.0F);
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

        switch (pose){
            case SWORD:
                this.leftLeg.xRot = 0f;
                this.rightLeg.xRot = 0f;
                this.leftArm.xRot = (float) (Math.PI /10 *-4);

                this.leftArm.yRot =  (float) (Math.PI /10 *1.5);

                this.rightArm.xRot = (float) (Math.PI /10 *-4);

                this.rightArm.yRot =  (float) (-Math.PI /10 *1.5);
                break;
            case TOOL:
                this.leftLeg.xRot = 0f;
                this.rightLeg.xRot = 0f;
                this.leftArm.xRot = (float) (Math.PI /10 *-3);

                this.leftArm.yRot =  (float) (Math.PI /10 *1.5);

                this.rightArm.xRot = (float) (Math.PI /10 *-3);

                this.rightArm.yRot =  (float) (-Math.PI /10 *1.5);
                break;
            case HOLDING:
                this.leftLeg.xRot = 0f;
                this.rightLeg.xRot = 0f;
                this.leftArm.xRot = (float) -(Math.PI / 4f);
                this.rightArm.xRot = (float) -(Math.PI / 4f);
                this.leftArm.yRot = 0;
                this.rightArm.yRot = 0;
                break;
            default:
            case STANDING:
                this.leftLeg.xRot = (float) (Math.PI/8f)*d;
                this.rightLeg.xRot = (float) (-Math.PI/8f)*d;
                this.leftArm.xRot = (float) (-Math.PI/8f)*d;
                this.rightArm.xRot = (float) (Math.PI/8f)*d;
                this.leftArm.yRot = 0;
                this.rightArm.yRot = 0;
                break;
            case CANDLE:
                this.leftLeg.xRot = 0f;
                this.rightLeg.xRot = 0f;
                this.leftArm.xRot = (float) -(Math.PI / 8f);
                this.rightArm.xRot = (float) -(Math.PI / 8f);
                this.leftArm.yRot = 0;
                this.rightArm.yRot = 0;
                break;
        }


        if(waving){
            this.rightArm.yRot = 0;
            this.rightArm.xRot = (float) (Math.PI);
            float f2 = ((float)Math.floorMod(ticks, 15L) + partialTricks) / 15.0F;
            this.rightArm.zRot = -0.5f-0.5f* Mth.sin(((float)Math.PI * 2F)*f2);
        }
        else{
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
        boolean slim = false;
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

