package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BuntingBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;


public class BuntingBlockTileRenderer implements BlockEntityRenderer<BuntingBlockTile> {

    private static ModelPart MODEL;
    private static ModelPart FLAG;
    private static ModelPart BOX;

    public BuntingBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        MODEL = context.bakeLayer(ClientRegistry.BUNTING_MODEL);
        FLAG = MODEL.getChild("flag");
        BOX = MODEL.getChild("box");
    }

    @Override
    public boolean shouldRender(BuntingBlockTile blockEntity, Vec3 cameraPos) {
        return blockEntity.shouldRenderFancy(cameraPos);
    }

    @Override
    public void render(BuntingBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn,
                       int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        BlockPos pos = tile.getBlockPos();
        long l = tile.getLevel().getGameTime();
        for (var e : tile.getBuntings().entrySet()) {
            renderBunting(e.getValue(), e.getKey(), partialTicks, poseStack,
                    null, bufferIn, combinedLightIn,
                    combinedOverlayIn, pos, l);
        }

        poseStack.popPose();

    }

    public static void renderBunting(DyeColor color, Direction dir, float partialTicks, PoseStack poseStack,
                                     @Nullable VertexConsumer vertexConsumer, @Nullable MultiBufferSource buffer,
                                     int combinedLightIn,
                                     int combinedOverlayIn, BlockPos pos, long l) {
        if (color != null) {
            poseStack.pushPose();

            var step = dir.step().mul(0.25f);
            poseStack.mulPose(RotHlpr.rot(dir));
            poseStack.translate(0, 0, -0.25);
            poseStack.scale(1, -1, -1);

            Material mat = ModMaterials.BUNTING_MATERIAL.get(color);
            VertexConsumer wrapped;
            if (buffer != null) {
                float h = ((float) Math.floorMod((long) (
                        (pos.getX() + step.x) * 7 +
                                (pos.getY() + step.y) * 9 +
                                (pos.getZ() + step.z) * 13) + l, 100L)
                        + partialTicks) / 100.0F;

                int i = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;
                FLAG.zRot = i * 0.01F * Mth.cos(6.2831855F * h) * 3.1415927F;

                wrapped = mat.buffer(buffer, RenderType::entityCutout);

            } else {
                FLAG.xRot = 0;
                wrapped = mat.sprite().wrap(vertexConsumer);
            }
            BOX.xScale = 1F;
            BOX.yScale = 1.1F;
            BOX.zScale = 1.1F;
            MODEL.render(poseStack, wrapped, combinedLightIn, combinedOverlayIn);
            poseStack.popPose();
        }
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("flag", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, 0, 0.0F, 7.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -2F, 0.0F, 0.0F, -1.5708F, 0.0F));

        partdefinition.addOrReplaceChild("box", CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-4F, -1.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 16);
    }

}