package net.mehvahdjukaar.supplementaries.client.renderers.entities.models;

import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class EndermanSkullModel extends SkullWithEyesModel {
    private final ModelPart hat;

    public EndermanSkullModel(ModelPart modelPart) {
        super(modelPart, ModTextures.ENDERMAN_HEAD_EYES);
        this.hat = modelPart.getChild("hat");
    }

    @Override
    public void setupAnim(float mouthAnim, float g, float h) {
        super.setupAnim(mouthAnim, g, h); //mouth anim is walk anim. we cant use it.
        this.hat.yRot = this.head.yRot;
    }

    public void setupJawAnimation(float mouthAnim) {
        this.head.y =  mouthAnim * -6;
        this.hat.y = 0;
    }

    //same as enderman. We do this because texture pack like to decapitate the enderman model. Looking at you, Fresh
    public static LayerDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.5F)), PartPose.ZERO);
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }
}
