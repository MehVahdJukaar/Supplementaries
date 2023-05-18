package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.supplementaries.common.block.IHangingSignExtension;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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

import java.util.List;

public class HangingSignRendererExtension {

    public static void render(SignBlockEntity tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource,
                              int packedLight, int packedOverlay, BlockState state,
                              HangingSignRenderer.HangingSignModel model, List<ModelPart> barModel,
                              Material material, Material extensionMaterial, SignRenderer renderer) {

        poseStack.pushPose();

        boolean notCeiling = !(state.getBlock() instanceof CeilingHangingSignBlock);
        boolean attached = state.hasProperty(BlockStateProperties.ATTACHED) && state.getValue(BlockStateProperties.ATTACHED);
        poseStack.translate(0.5, 0.875, 0.5);
        if (attached) {
            float f = -RotationSegment.convertToDegrees(state.getValue(CeilingHangingSignBlock.ROTATION));
            poseStack.mulPose(Axis.YP.rotationDegrees(f));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(getSignAngle(state, notCeiling)));
        }


        model.evaluateVisibleParts(state);
        VertexConsumer vertexConsumer = material.buffer(bufferSource, model::renderType);
        IHangingSignExtension sign = (IHangingSignExtension)tile;

        poseStack.scale(1, -1, -1);

        boolean visible = model.plank.visible;


        poseStack.pushPose();

        model.plank.visible = false;
        poseStack.mulPose(Axis.XP.rotationDegrees(sign.getSwayingAnimation().getSwingAngle(partialTicks)));
        poseStack.translate(0,0.25,0);

        model.root.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        model.plank.visible = visible;


        poseStack.pushPose();
        poseStack.scale(1, -1, -1);
        //this dumb method always pops but doesnt push
        renderer.renderSignText(tile, poseStack, bufferSource, packedLight, 1.0F);

        poseStack.popPose();


        poseStack.translate(0,0.25,0);

        if (visible) {
            model.plank.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        }

        ModBlockProperties.PostType right = sign.getRightAttachment();
        ModBlockProperties.PostType left = sign.getLeftAttachment();

        VertexConsumer vc2 = null;
        if (right != null || left != null) {
            vc2 = extensionMaterial.buffer(bufferSource, model::renderType);
        }
        if (left != null) {
            poseStack.pushPose();
            poseStack.translate(1, 0, 0);
            barModel.get(left.ordinal()).render(poseStack, vc2, packedLight, packedOverlay);
            poseStack.popPose();
        }
        if (right != null) {
            poseStack.pushPose();
            poseStack.translate(-right.getOffset(), 0, 0);
            barModel.get(right.ordinal()).render(poseStack, vc2, packedLight, packedOverlay);
            poseStack.popPose();
        }


        poseStack.popPose();
    }

    private static float getSignAngle(BlockState state, boolean attachedToWall) {
        return attachedToWall ? -(state.getValue(WallSignBlock.FACING)).toYRot() : -((state.getValue(CeilingHangingSignBlock.ROTATION) * 360) / 16.0F);
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("extension_6", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(4.0F, -8.0F, -2.0F, 2.0F, 6.0F, 4.0F),
                PartPose.rotation(0.0F, 0.0F, -1.5708F));
        partDefinition.addOrReplaceChild("extension_5", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(4.0F, -8.0F, -2.0F, 2.0F, 5.0F, 4.0F),
                PartPose.rotation(0.0F, 0.0F, -1.5708F));
        partDefinition.addOrReplaceChild("extension_4", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(4.0F, -8.0F, -2.0F, 2.0F, 4.0F, 4.0F),
                PartPose.rotation(0.0F, 0.0F, -1.5708F));
        partDefinition.addOrReplaceChild("extension_3", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(4.0F, -8.0F, -2.0F, 2.0F, 3.0F, 4.0F),
                PartPose.rotation(0.0F, 0.0F, -1.5708F));
        return LayerDefinition.create(meshDefinition, 16, 16);
    }
}
