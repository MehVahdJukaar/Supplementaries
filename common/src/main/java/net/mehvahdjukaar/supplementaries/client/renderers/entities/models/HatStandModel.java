package net.mehvahdjukaar.supplementaries.client.renderers.entities.models;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
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

            GenericAnimationStuff.animate(this, entity.skibidiAnimation, SKIBIDI, ageInTicks, 1);

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

        zAngle += entity.animation.getAngle(partialTick) * Mth.DEG_TO_RAD;


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


    private static final AnimationDefinition SKIBIDI = AnimationDefinition.Builder.withLength(5.208343f)
            .addAnimation("dummy_head",
                    new AnimationChannel(AnimationChannel.Targets.POSITION,
                            new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.041676664f, KeyframeAnimations.posVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.20834334f, KeyframeAnimations.posVec(0f, 13f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.375f, KeyframeAnimations.posVec(0f, 8f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, KeyframeAnimations.posVec(0f, 9f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.875f, KeyframeAnimations.posVec(0f, 9f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.9583435f, KeyframeAnimations.posVec(0f, 12f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.0416765f, KeyframeAnimations.posVec(0f, 19f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.125f, KeyframeAnimations.posVec(0f, 21f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.1676665f, KeyframeAnimations.posVec(0f, 21f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.25f, KeyframeAnimations.posVec(0f, 19f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.3433335f, KeyframeAnimations.posVec(0f, 12f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.4167665f, KeyframeAnimations.posVec(0f, 9f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.5f, KeyframeAnimations.posVec(0f, 9f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.5416765f, KeyframeAnimations.posVec(0f, 11f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.5834335f, KeyframeAnimations.posVec(0f, 20f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.625f, KeyframeAnimations.posVec(0f, 22f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.75f, KeyframeAnimations.posVec(0f, 22f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.7916765f, KeyframeAnimations.posVec(0f, 20f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.8343335f, KeyframeAnimations.posVec(0f, 11f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.875f, KeyframeAnimations.posVec(0f, 9f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4f, KeyframeAnimations.posVec(0f, 9f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.041677f, KeyframeAnimations.posVec(0f, 11f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.083433f, KeyframeAnimations.posVec(0f, 21f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.125f, KeyframeAnimations.posVec(0f, 23f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.25f, KeyframeAnimations.posVec(0f, 23f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.291677f, KeyframeAnimations.posVec(0f, 21f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.343333f, KeyframeAnimations.posVec(0f, 11f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.625f, KeyframeAnimations.posVec(0f, 9f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(5.083433f, KeyframeAnimations.posVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("dummy_head",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.08343333f, KeyframeAnimations.degreeVec(-6f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.20834334f, KeyframeAnimations.degreeVec(-27.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.3433333f, KeyframeAnimations.degreeVec(-6f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.4167667f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(2.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.625f, KeyframeAnimations.degreeVec(10f, 5f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6766666f, KeyframeAnimations.degreeVec(2.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(2.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.9583434f, KeyframeAnimations.degreeVec(10f, -5f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1f, KeyframeAnimations.degreeVec(2.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0834333f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.2083433f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.2916767f, KeyframeAnimations.degreeVec(-3f, -10f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.3433333f, KeyframeAnimations.degreeVec(-10f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.375f, KeyframeAnimations.degreeVec(-3f, 10f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.4583433f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5416767f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.6766667f, KeyframeAnimations.degreeVec(-15f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.7083433f, KeyframeAnimations.degreeVec(-15f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.75f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.8343333f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.875f, KeyframeAnimations.degreeVec(-15f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.9167667f, KeyframeAnimations.degreeVec(-15f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.9583433f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.0416765f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.3433335f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.4167665f, KeyframeAnimations.degreeVec(2.5f, 0f, -5f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.4583435f, KeyframeAnimations.degreeVec(10f, 5f, -6f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, KeyframeAnimations.degreeVec(2.5f, 0f, -5f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5834335f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.6766665f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.75f, KeyframeAnimations.degreeVec(2.5f, 0f, 5f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.7916765f, KeyframeAnimations.degreeVec(10f, -5f, 6f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.8343335f, KeyframeAnimations.degreeVec(2.5f, 0f, 5f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.875f, KeyframeAnimations.degreeVec(2.5f, 0f, 5f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.9167665f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.9583435f, KeyframeAnimations.degreeVec(-0.19f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.0416765f, KeyframeAnimations.degreeVec(0f, 0f, -42f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.1676665f, KeyframeAnimations.degreeVec(20f, 0f, -173f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.2916765f, KeyframeAnimations.degreeVec(17f, 0f, -322f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.4167665f, KeyframeAnimations.degreeVec(0f, 0f, -360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.5416765f, KeyframeAnimations.degreeVec(0f, 0f, -360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.5834335f, KeyframeAnimations.degreeVec(-5.5f, 0f, -360f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.7916765f, KeyframeAnimations.degreeVec(-5.5f, 0f, -360f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.8343335f, KeyframeAnimations.degreeVec(0f, 0f, -360f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.041677f, KeyframeAnimations.degreeVec(0f, 0f, -360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.083433f, KeyframeAnimations.degreeVec(-5.5f, 0f, -360f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.291677f, KeyframeAnimations.degreeVec(-5.5f, 0f, -360f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.343333f, KeyframeAnimations.degreeVec(0f, 0f, -360f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.541677f, KeyframeAnimations.degreeVec(0f, 0f, -360f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("neck_joint",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(0f, 10f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.6766666f, KeyframeAnimations.degreeVec(0f, 10f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(0.75f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, -10f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1f, KeyframeAnimations.degreeVec(0f, -10f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.0834333f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5834333f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 2.5f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.7083433f, KeyframeAnimations.degreeVec(0f, 2.5f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.75f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.8343333f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.875f, KeyframeAnimations.degreeVec(0f, -2.5f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(1.9583433f, KeyframeAnimations.degreeVec(0f, -2.5f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.3433335f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.4167665f, KeyframeAnimations.degreeVec(0f, 10f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5f, KeyframeAnimations.degreeVec(0f, 10f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.5834335f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.6766665f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.75f, KeyframeAnimations.degreeVec(0f, -10f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.8343335f, KeyframeAnimations.degreeVec(0f, -10f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2.9167665f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.0834335f, KeyframeAnimations.degreeVec(0f, 0f, 5f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.25f, KeyframeAnimations.degreeVec(0f, 0f, -4f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.375f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.5416765f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.5834335f, KeyframeAnimations.degreeVec(11.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(3.7916765f, KeyframeAnimations.degreeVec(11.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.8343335f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.041677f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.083433f, KeyframeAnimations.degreeVec(11.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4.291677f, KeyframeAnimations.degreeVec(11.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.343333f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.541677f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(5.083433f, KeyframeAnimations.degreeVec(0f, 356f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM))).build();
}
