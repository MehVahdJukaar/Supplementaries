package net.mehvahdjukaar.supplementaries.client.renderers.entities.models;

import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class PlundererModel extends IllagerModel<PlundererEntity> {
    private final ModelPart skirt;

    public PlundererModel(ModelPart root) {
        super(root);
        this.getHat().visible = true;
        this.skirt = root.getChild("body").getChild("skirt");
    }

    @Override
    public void prepareMobModel(PlundererEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);

        this.skirt.visible = !entity.isPassenger();
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        partDefinition2.addOrReplaceChild("hat", CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.45F))
                        .texOffs(0, 72)
                        .addBox(-5.0F, -11.0F, -4.0F, 10.0F, 4.0F, 8.0F, new CubeDeformation(0.5F)),

                PartPose.ZERO);
        partDefinition2.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition bodyPart = partDefinition.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 22)
                        .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F)
                        .texOffs(0, 40)
                        .addBox(-4.0F, 0.0F, -3.0F, 8.0F, 14.0F, 6.0F, new CubeDeformation(0.5F)),
                PartPose.ZERO);
        bodyPart.addOrReplaceChild("skirt", CubeListBuilder.create()
                        .texOffs(0, 60)
                        .addBox(-4.0F, 15.0F, -3F, 8.0F, 6.0F, 6.0F, new CubeDeformation(0.5F)),

                PartPose.ZERO);


        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild("arms", CubeListBuilder.create()
                        .texOffs(44, 22)
                        .addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F)
                        .texOffs(40, 38)
                        .addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, -1.0F, -0.75F, 0.0F, 0.0F));
        partDefinition3.addOrReplaceChild("left_shoulder", CubeListBuilder.create()
                .texOffs(44, 22)
                .mirror()
                .addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.ZERO);
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 20)
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-2.0F, 12.0F, 0.0F));
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 20)
                .mirror()
                .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(2.0F, 12.0F, 0.0F));
        partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create()
                        .texOffs(40, 46)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));
        partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create()
                        .texOffs(40, 46)
                        .mirror()
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(5.0F, 2.0F, 0.0F));
        return LayerDefinition.create(meshDefinition, 64, 128);
    }

}
