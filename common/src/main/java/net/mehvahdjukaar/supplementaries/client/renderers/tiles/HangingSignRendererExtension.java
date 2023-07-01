package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.TextUtils;
import net.mehvahdjukaar.supplementaries.common.block.IExtendedHangingSign;
import net.mehvahdjukaar.supplementaries.common.block.ModBlockProperties;
import net.minecraft.client.Minecraft;
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
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class HangingSignRendererExtension {


    public static void render(SignBlockEntity tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource,
                              int packedLight, int packedOverlay, BlockState state,
                              HangingSignRenderer.HangingSignModel model, List<ModelPart> barModel, ModelPart chains,
                              Material material, Material extensionMaterial, SignRenderer renderer,
                              float colorMult) { //color mult for FD

        poseStack.pushPose();

        boolean wallSign = !(state.getBlock() instanceof CeilingHangingSignBlock);
        boolean attached = !wallSign && state.hasProperty(BlockStateProperties.ATTACHED) && state.getValue(BlockStateProperties.ATTACHED);
        poseStack.translate(0.5, 0.875, 0.5);

        Quaternionf yaw;
        if (attached) {
            yaw = Axis.YP.rotationDegrees(-RotationSegment.convertToDegrees(state.getValue(CeilingHangingSignBlock.ROTATION)));
        } else {
            yaw = Axis.YP.rotationDegrees(getSignAngle(state, wallSign));
        }
        poseStack.mulPose(yaw);

        model.evaluateVisibleParts(state);
        VertexConsumer vertexConsumer = material.buffer(bufferSource, model::renderType);
        var sign = ((IExtendedHangingSign) tile).getExtension();

        poseStack.scale(1, -1, -1);
        //TODO: ceiling banner rot

        boolean visible = model.plank.visible;
        boolean visibleC = model.normalChains.visible;
        model.plank.visible = false;
        if (wallSign) {
            model.normalChains.visible = false;
        }

        poseStack.pushPose();

        Quaternionf pitch = new Quaternionf();
        if (((IExtendedHangingSign) tile).getExtension().canSwing()) {
            float rot = sign.animation.getAngle(partialTicks);

            if(!wallSign && attached){
                //y swing
                pitch = Axis.YP.rotationDegrees(rot);
            }else{
                pitch = Axis.XP.rotationDegrees(rot);

            }

            if(!wallSign){
                poseStack.translate(0,-0.125,0);
            }
            poseStack.mulPose(pitch);
            if(!wallSign){
                poseStack.translate(0,0.125,0);
            }
        }


        Vector3f norm = Direction.SOUTH.step().rotate(pitch).rotate(yaw);


        //model
        poseStack.pushPose();

        poseStack.translate(0, 0.25, 0);


        model.root.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        if (wallSign) {
            chains.render(poseStack, vertexConsumer, packedLight, packedOverlay); //shorter chains
            model.normalChains.visible = visibleC;
        }
        model.plank.visible = visible;

        poseStack.popPose();


        poseStack.scale(1, -1, -1);

        Minecraft mc = Minecraft.getInstance();
        var camera = mc.gameRenderer.getMainCamera();
        var font = mc.font;
        boolean filtered = mc.isTextFilteringEnabled();

        LOD lod = new LOD(camera, tile.getBlockPos());

        var off = renderer.getTextOffset();


        poseStack.pushPose();
        renderer.translateSignText(poseStack, true, off);
        TextUtils.renderSignText(tile.getFrontText(), font, poseStack, bufferSource, packedLight,
                norm, lod, filtered, tile.getTextLineHeight(), tile.getMaxTextLineWidth(),
                colorMult);
        poseStack.popPose();

        poseStack.pushPose();
        renderer.translateSignText(poseStack, false, off);
        TextUtils.renderSignText(tile.getBackText(), font, poseStack, bufferSource, packedLight,
                norm.mul(-1), lod, filtered, tile.getTextLineHeight(), tile.getMaxTextLineWidth(),
                colorMult);
        poseStack.popPose();


        //Item item = Items.SKULL_BANNER_PATTERN;
        //renderBannerPattern(tile, poseStack, bufferSource, packedLight, item);

        poseStack.popPose();

        poseStack.translate(0, 0.25, 0);


        //Straight stuff


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
            poseStack.mulPose(RotHlpr.Y180);
            poseStack.translate(1, 0, 0);
            barModel.get(right.ordinal()).render(poseStack, vc2, packedLight, packedOverlay);
            poseStack.popPose();
        }


        poseStack.popPose();
    }
/*
    private static void renderBannerPattern(SignBlockEntity tile, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Item item) {
        if (item instanceof BannerPatternItem bannerPatternItem) {
            poseStack.translate(0, 5/16f, 0);

            float scale =0.75f;
            poseStack.scale(scale, -scale, -1);


            Material renderMaterial = ModMaterials.getFlagMaterialForPatternItem(bannerPatternItem);
            if (renderMaterial != null) {

                VertexConsumer builder = renderMaterial.buffer(bufferSource, RenderType::itemEntityTranslucentCull);


                float[] color = tile.getColor().getTextureDiffuseColors();
                float b = color[2];
                float g = color[1];
                float r = color[0];
                int light = packedLight;
                if (tile.hasGlowingText()) {
                    light = LightTexture.FULL_BRIGHT;
                }

                int lu = light & '\uffff';
                int lv = light >> 16 & '\uffff';
                for (int v = 0; v < 2; v++) {
                    VertexUtils.addQuadSide(builder, poseStack, -0.4375F, -0.4375F, 0.07f,
                            0.4375F, 0.4375F, 0.07f,
                            0.15625f, 0.0625f, 0.5f + 0.09375f, 1 - 0.0625f, r, g, b, 1, lu, lv, 0, 0, 1, renderMaterial.sprite());

                    poseStack.mulPose(RotHlpr.Y180);
                }
            }
        }
    }



    public static void renderItem(){
                        BakedModel model = itemRenderer.getModel(stack, tile.getLevel(), null, 0);
                    for (int v = 0; v < 2; v++) {
                        poseStack.pushPose();
                        poseStack.scale(0.75f, 0.75f, 0.75f);
                        poseStack.translate(0, 0, -0.1);
                        //poseStack.mulPose(Const.Y180);
                        itemRenderer.render(stack, ItemDisplayContext.FIXED, true, poseStack, bufferIn, combinedLightIn,
                                combinedOverlayIn, model);
                        poseStack.popPose();

                        poseStack.mulPose(RotHlpr.Y180);
                        poseStack.scale(0.9995f, 0.9995f, 0.9995f);
                    }
                    }


    */

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

    public static LayerDefinition createChainMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild("chainL1", CubeListBuilder.create().texOffs(0, 7).addBox(-1.5F, 1.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, -0.7853982F, 0.0F));
        root.addOrReplaceChild("chainL2", CubeListBuilder.create().texOffs(6, 7).addBox(-1.5F, 1.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, 0.7853982F, 0.0F));
        root.addOrReplaceChild("chainR1", CubeListBuilder.create().texOffs(0, 7).addBox(-1.5F, 1.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, -0.7853982F, 0.0F));
        root.addOrReplaceChild("chainR2", CubeListBuilder.create().texOffs(6, 7).addBox(-1.5F, 1.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, 0.7853982F, 0.0F));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }
}
