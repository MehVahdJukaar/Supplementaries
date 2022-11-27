package net.mehvahdjukaar.supplementaries.client.renderers.entities.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelPart;

public class EndermanSkullModel extends SkullModel {
    private final ModelPart hat;

    public EndermanSkullModel(ModelPart modelPart) {
        super(modelPart);
        this.hat = modelPart.getChild("hat");
    }

    @Override
    public void setupAnim(float mouthAnim, float g, float h) {
        super.setupAnim(mouthAnim, g, h);
        this.head.y = mouthAnim * -6;
        this.hat.y = 0;
        this.hat.yRot = this.head.yRot;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        this.head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.hat.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
