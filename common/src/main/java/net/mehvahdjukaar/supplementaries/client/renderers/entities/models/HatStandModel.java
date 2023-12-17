package net.mehvahdjukaar.supplementaries.client.renderers.entities.models;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class HatStandModel extends HumanoidModel<HatStandEntity> implements IRootModel{


    private final ModelPart basePlate;
    private final ModelPart neck;
    private final ModelPart root;

    public HatStandModel(ModelPart modelPart) {
        super(modelPart);
        this.basePlate = modelPart.getChild("base_plate");
        this.root = modelPart;
        this.neck = head.getChild("neck");
    }

    @Override
    public ModelPart getRoot() {
        return root;
    }

    public static LayerDefinition createMesh() {
        int f = 20;
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, f);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("base_plate", CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-6.0F, 3, -6.0F, 12.0F, 1.0F, 12.0F),
                PartPose.offset(0.0F, f, 0.0F));

        var head = partDefinition.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, f, 0.0F));
        head.addOrReplaceChild("neck", CubeListBuilder.create()
                                .texOffs(0, 0)
                                .addBox(-1, -0, -1, 2, 3, 2),
                        PartPose.offset(0.0F, 0, 0));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    public static LayerDefinition createArmorMesh() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(new CubeDeformation(1), 0);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(HatStandEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        /*
        head.resetPose();
        neck.resetPose();

        float originalY = head.y;
        GenericAnimationStuff.animate(this, entity.skibidiAnimation, SKIBIDI, ageInTicks, 2);
        float neckInc = originalY -head.y ;
        float neckH = 3;
        neck.yScale = (neckH+neckInc)/neckH;*/
    }


    @Override
    public void prepareMobModel(HatStandEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        this.basePlate.visible = !entity.isNoBasePlate();

        float xAngle = Mth.DEG_TO_RAD * entity.getHeadPose().getX();
        this.head.yRot = Mth.DEG_TO_RAD * entity.getHeadPose().getY();
        float zAngle = Mth.DEG_TO_RAD * entity.getHeadPose().getZ();
        this.hat.copyFrom(this.head);
        this.basePlate.xRot = 0.0F;
        this.basePlate.yRot = Mth.DEG_TO_RAD * -Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        this.basePlate.zRot = 0.0F;


        zAngle += entity.animation.getAngle(partialTick) * Mth.DEG_TO_RAD;

        this.head.setPos(0.0F, 20.0F, 0.0F);
        //so we rotate below neck
        this.rotateModel(this.head, 0, 23, 0, xAngle, zAngle);
    }

    //don't touch. it just works. dummmmmy code
    public void rotateModel(ModelPart model, float nrx, float nry, float nrz, float xAngle, float zAngle) {
        Vec3 oldRot = new Vec3(model.x, model.y, model.z);
        Vec3 actualRot = new Vec3(nrx, nry, nrz);

        Vec3 newRot = actualRot.add(oldRot.subtract(actualRot)
                .xRot(-xAngle)
                .zRot(-zAngle));

        model.setPos((float) newRot.x(), (float) newRot.y(), (float) newRot.z());
        model.xRot = xAngle;
        model.zRot = zAngle;
    }


    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.head, this.basePlate);
    }

    @Override
    public ModelPart getHead() {
        return super.getHead();
    }












    public static final AnimationDefinition SKIBIDI = AnimationDefinition.Builder.withLength(4.208343f)
            .addAnimation("head",
                    new AnimationChannel(AnimationChannel.Targets.POSITION,
                            new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.125f, KeyframeAnimations.posVec(0f, 9f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 9f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.0416767f, KeyframeAnimations.posVec(0f, 17f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.2916767f, KeyframeAnimations.posVec(0f, 17f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.5f, KeyframeAnimations.posVec(0f, 9f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 6f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.875f, KeyframeAnimations.posVec(0f, 17f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.125f, KeyframeAnimations.posVec(0f, 17f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2916765f, KeyframeAnimations.posVec(0f, 6f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.5834335f, KeyframeAnimations.posVec(0f, 17f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.8343335f, KeyframeAnimations.posVec(0f, 17f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3f, KeyframeAnimations.posVec(0f, 6f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.125f, KeyframeAnimations.posVec(0f, 6f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.4167665f, KeyframeAnimations.posVec(0f, 17f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.5416765f, KeyframeAnimations.posVec(0f, 17f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.125f, KeyframeAnimations.posVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("head",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.125f, KeyframeAnimations.degreeVec(-17.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.2083433f, KeyframeAnimations.degreeVec(0f, 0f, 180f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.4583433f, KeyframeAnimations.degreeVec(0f, 0f, 360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.75f, KeyframeAnimations.degreeVec(0f, 0f, 360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.875f, KeyframeAnimations.degreeVec(-18f, 0f, 360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2916765f, KeyframeAnimations.degreeVec(0f, 0f, 360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.5834335f, KeyframeAnimations.degreeVec(-18f, 0f, 360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3f, KeyframeAnimations.degreeVec(0f, 0f, 360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.25f, KeyframeAnimations.degreeVec(0f, 0f, 360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.75f, KeyframeAnimations.degreeVec(0f, -180f, 360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(4.125f, KeyframeAnimations.degreeVec(0f, -360f, 360f),
                                    AnimationChannel.Interpolations.LINEAR)))
            .addAnimation("neck",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.125f, KeyframeAnimations.degreeVec(17.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.2083433f, KeyframeAnimations.degreeVec(0f, 0f, -180f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.4583433f, KeyframeAnimations.degreeVec(0f, 0f, -360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.75f, KeyframeAnimations.degreeVec(0f, 0f, -360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(1.875f, KeyframeAnimations.degreeVec(18f, 0f, -360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.2916765f, KeyframeAnimations.degreeVec(0f, 0f, -360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(2.5834335f, KeyframeAnimations.degreeVec(18f, 0f, -360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3f, KeyframeAnimations.degreeVec(0f, 0f, -360f),
                                    AnimationChannel.Interpolations.LINEAR),
                            new Keyframe(3.25f, KeyframeAnimations.degreeVec(0f, 0f, -360f),
                                    AnimationChannel.Interpolations.LINEAR))).build();}
