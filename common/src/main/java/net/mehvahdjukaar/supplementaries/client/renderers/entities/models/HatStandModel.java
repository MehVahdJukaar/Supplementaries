package net.mehvahdjukaar.supplementaries.client.renderers.entities.models;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.funny.SkibidiAnimations;
import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class HatStandModel extends HumanoidModel<HatStandEntity> implements IRootModel {


    private final ModelPart basePlate;
    private final ModelPart neck;
    private final ModelPart neckJoint;
    private final ModelPart root;

    private final ModelPart dummyHead;

    public HatStandModel(ModelPart modelPart) {
        super(modelPart);
        this.basePlate = modelPart.getChild("base_plate");
        this.root = modelPart;
        this.neckJoint = modelPart.getChild("neck_joint");
        this.neck = neckJoint.getChild("neck");

        this.dummyHead = neckJoint.getChild("dummy_head");
        this.dummyHead.visible = false;
    }

    @Override
    public ModelPart getRoot() {
        return root;
    }

    @Override
    public ModelPart getHead() {
        return head;
    }


    public static LayerDefinition createMesh() {
        int f = 20;
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("base_plate", CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-6.0F, 3, -6.0F, 12.0F, 1.0F, 12.0F),
                PartPose.offset(0.0F, f, 0.0F));

        partDefinition.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, f, 0.0F));


        var neck = partDefinition.addOrReplaceChild("neck_joint", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0, 0));

        neck.addOrReplaceChild("neck", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1, -3, -1, 2, 3, 2),
                PartPose.ZERO);


        neck.addOrReplaceChild("dummy_head", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, -3, 0.0F));


        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    public static LayerDefinition createArmorMesh() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(new CubeDeformation(1), 0);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(HatStandEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        Pose pose = entity.getPose();
        if (pose != Pose.STANDING) {

            GenericAnimationStuff.animate(this, entity.skibidiAnimation, SkibidiAnimations.FLUSH, ageInTicks, 1.24f);

            dummyHead.y += pose == Pose.SPIN_ATTACK ? 0 : 9;
            dummyHead.visible = false;
        }

        // distance between 2 pivots
        Vector4f newPivot = new Vector4f(dummyHead.x, dummyHead.y, dummyHead.z, 1);

        PoseStack poseStack = new PoseStack();

        translateAndRotate(neckJoint, poseStack);
        poseStack.last().pose().transform(newPivot);
        var in = head.getInitialPose();
        head.offsetPos(new Vector3f(newPivot.x - in.x, newPivot.y - in.y, newPivot.z - in.z));
        head.setRotation(neckJoint.xRot + dummyHead.xRot,
                neckJoint.yRot + dummyHead.yRot, neckJoint.zRot + dummyHead.zRot);

        neck.yScale = (-dummyHead.y) / 3;
    }


    @Override
    public void prepareMobModel(HatStandEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {

        this.basePlate.visible = !entity.isNoBasePlate();
        this.basePlate.xRot = 0.0F;
        this.basePlate.yRot = Mth.DEG_TO_RAD * -Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        this.basePlate.zRot = 0.0F;


        float xAngle = Mth.DEG_TO_RAD * entity.getHeadPose().getX();
        this.head.yRot = Mth.DEG_TO_RAD * entity.getHeadPose().getY();
        float zAngle = Mth.DEG_TO_RAD * entity.getHeadPose().getZ();
        this.hat.copyFrom(this.head);

        zAngle += entity.swingAnimation.getAngle(partialTick) * Mth.DEG_TO_RAD;


        head.resetPose();
        neck.resetPose();
        neckJoint.resetPose();
        dummyHead.resetPose();
        neckJoint.y += 23f;

        neckJoint.zRot += zAngle;
        neckJoint.xRot += xAngle;


    }

    public void translateAndRotate(ModelPart modelPart, PoseStack poseStack) {
        poseStack.translate(modelPart.x, modelPart.y, modelPart.z);
        if (modelPart.xRot != 0.0F || modelPart.yRot != 0.0F || modelPart.zRot != 0.0F) {
            poseStack.mulPose((new Quaternionf()).rotationZYX(modelPart.zRot, modelPart.yRot, modelPart.xRot));
        }

        if (modelPart.xScale != 1.0F || modelPart.yScale != 1.0F || modelPart.zScale != 1.0F) {
            poseStack.scale(modelPart.xScale, modelPart.yScale, modelPart.zScale);
        }

    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.head, this.basePlate, this.neckJoint);
    }

}
