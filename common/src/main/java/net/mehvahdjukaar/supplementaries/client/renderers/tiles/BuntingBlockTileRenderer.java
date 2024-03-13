package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;


public class BuntingBlockTileRenderer implements BlockEntityRenderer<BuntingBlockTile> {


    private final ModelPart model;

    public BuntingBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.model = context.bakeLayer(ClientRegistry.BUNTING_MODEL);
    }

    @Override
    public void render(BuntingBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn,
                       int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        BlockPos pos = tile.getBlockPos();
        long l = tile.getLevel().getGameTime();

        for (var e : tile.getBuntings().entrySet()) {
            DyeColor color = e.getValue();
            if (color != null) {
                poseStack.pushPose();

                Direction dir = e.getKey();
                var step = dir.step().mul(0.25f);
                poseStack.mulPose(RotHlpr.rot(dir));
                poseStack.translate(0, 0, -0.25);
                poseStack.scale(1, -1, -1);

                float h = ((float) Math.floorMod((long) (
                        (pos.getX() + step.x) * 7 +
                                (pos.getY() + step.y) * 9 +
                                (pos.getZ() + step.z) * 13) + l, 100L)
                        + partialTicks) / 100.0F;

                int i = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1 : -1;
                this.model.zRot = i * 0.01F * Mth.cos(6.2831855F * h) * 3.1415927F;

                model.render(poseStack, ModMaterials.BUNTING_MATERIAL.get(color).buffer(bufferIn, RenderType::entityCutout),
                        combinedLightIn, combinedOverlayIn);
                poseStack.popPose();
            }
        }
        poseStack.popPose();

    }

    public static LayerDefinition createMeshPixelPerfect() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bunting", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-3.0F, -9.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-3.0F, -7.0F, 0.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 5, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

         partdefinition.addOrReplaceChild("flag", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -7.0F, 0.0F, 7.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
                 PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

       partdefinition.addOrReplaceChild("box", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-3.5F, -9.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)),
               PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 16);
    }

}