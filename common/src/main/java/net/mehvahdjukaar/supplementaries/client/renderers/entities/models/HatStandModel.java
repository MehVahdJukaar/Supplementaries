package net.mehvahdjukaar.supplementaries.client.renderers.entities.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.common.entities.HatStandEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class HatStandModel extends EntityModel<HatStandEntity> {

    private final ModelPart head;
    private final ModelPart hat;

    public HatStandModel(ModelPart modelPart) {
        super();
        this.head = modelPart.getChild("head");
        this.hat = modelPart.getChild("hat");
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = 0;
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -0.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 0.0F + f, 0.0F));
        partDefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -0.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE.extend(0.5F)), PartPose.offset(0.0F, 0.0F + f, 0.0F));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    /**
     * Sets this entity's model rotation angles
     */
    @Override
    public void setupAnim(HatStandEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.xRot = Mth.DEG_TO_RAD * entity.getHeadPose().getX();
        this.head.yRot = Mth.DEG_TO_RAD * entity.getHeadPose().getY();
        this.head.zRot = Mth.DEG_TO_RAD * entity.getHeadPose().getZ();
        this.hat.copyFrom(this.head);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        hat.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
