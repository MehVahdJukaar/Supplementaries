package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.common.block.IHangingSignExtension;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.List;
import java.util.Objects;

public class HangingSignRendererExtension {

    public static void render(SignBlockEntity tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                              int packedLight, int packedOverlay, BlockState state,
                              HangingSignRenderer.HangingSignModel model, List<ModelPart> barModel,
                              Material signMaterial,Material extensionMaterial,  SignRenderer renderer) {
        poseStack.pushPose();
        boolean notCeiling = !(state.getBlock() instanceof CeilingHangingSignBlock);
        boolean attached = state.hasProperty(BlockStateProperties.ATTACHED) && state.getValue(BlockStateProperties.ATTACHED);
        poseStack.translate(0.5, 0.9375, 0.5);
        float f;
        if (attached) {
            f = -RotationSegment.convertToDegrees(state.getValue(CeilingHangingSignBlock.ROTATION));
            poseStack.mulPose(Axis.YP.rotationDegrees(f));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(getSignAngle(state, notCeiling)));
        }

        poseStack.translate(0.0F, -0.3125F, 0.0F);
        model.evaluateVisibleParts(state);
        f = 1.0F;
        renderSign(poseStack, bufferSource, packedLight, packedOverlay,
                signMaterial,extensionMaterial, model, barModel, (IHangingSignExtension) tile);
        renderer.renderSignText(tile, poseStack, bufferSource, packedLight, 1.0F);
    }

    private static float getSignAngle(BlockState state, boolean attachedToWall) {
        return attachedToWall ? -(state.getValue(WallSignBlock.FACING)).toYRot() : -((state.getValue(CeilingHangingSignBlock.ROTATION) * 360) / 16.0F);
    }

    static void renderSign(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                           Material material, Material extensionMaterial,
                           HangingSignRenderer.HangingSignModel model, List<ModelPart> extension,
                           IHangingSignExtension tile) {
        poseStack.pushPose();
        poseStack.scale(1, -1, -1);
        VertexConsumer vertexConsumer = material.buffer(bufferSource, model::renderType);
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0f);

        ModBlockProperties.PostType right = tile.getRightAttachment();
        ModBlockProperties.PostType left = tile.getLeftAttachment();

        VertexConsumer vc2 = null;
        if(right != null || left != null){
            vc2 = extensionMaterial.buffer(bufferSource, model::renderType);
        }
        if(left != null) {
            poseStack.pushPose();
            poseStack.translate(1, 0, 0);
            extension.get(left.ordinal()).render(poseStack, vc2, packedLight, packedOverlay);
            poseStack.popPose();
        }
        if(right != null) {
            poseStack.pushPose();
            poseStack.translate(-right.getOffset(), 0, 0);
            extension.get(right.ordinal()).render(poseStack, vc2, packedLight, packedOverlay);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("extension_6", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(4.0F, -8.0F, -2.0F, 2.0F, 6.0F, 4.0F),
                PartPose.rotation( 0.0F, 0.0F, -1.5708F));
        partDefinition.addOrReplaceChild("extension_5", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(4.0F, -8.0F, -2.0F, 2.0F, 5.0F, 4.0F),
                PartPose.rotation( 0.0F, 0.0F, -1.5708F));
        partDefinition.addOrReplaceChild("extension_4", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(4.0F, -8.0F, -2.0F, 2.0F, 4.0F, 4.0F),
                PartPose.rotation( 0.0F, 0.0F, -1.5708F));
        partDefinition.addOrReplaceChild("extension_3", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(4.0F, -8.0F, -2.0F, 2.0F, 3.0F, 4.0F),
                PartPose.rotation( 0.0F, 0.0F, -1.5708F));
        return LayerDefinition.create(meshDefinition, 16, 16);
    }
}
