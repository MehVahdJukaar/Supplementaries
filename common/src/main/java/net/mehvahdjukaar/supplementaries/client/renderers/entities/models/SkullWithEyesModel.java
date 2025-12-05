package net.mehvahdjukaar.supplementaries.client.renderers.entities.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;

public class SkullWithEyesModel extends SkullModel {
    private final ResourceLocation eyesTexture;

    public SkullWithEyesModel(ModelPart root, ResourceLocation eyesTexture) {
        super(root);
        this.eyesTexture = eyesTexture;
    }

    public void renderEyes(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int i) {
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.eyes(eyesTexture));
        this.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, i);
    }
}
