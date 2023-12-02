package net.mehvahdjukaar.supplementaries.client.renderers.entities.models;

import com.google.common.collect.ImmutableList;
import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class HatStandModel extends HumanoidModel<HatStandEntity> {


    private final ModelPart basePlate;

    public HatStandModel(ModelPart modelPart) {
        super(modelPart);
        this.basePlate = modelPart.getChild("base_plate");
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
                        .addBox(-1, -0, -1, 2, 3, 2)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 3.0F + f, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    public static LayerDefinition createArmorMesh() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(new CubeDeformation(1), 0);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(HatStandEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
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
}
