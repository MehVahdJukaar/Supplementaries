package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BuntingBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;


public class BuntingBlockTileRenderer implements BlockEntityRenderer<BuntingBlockTile> {


    private final ModelPart model;

    public BuntingBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.model = context.bakeLayer(ClientRegistry.BUNTING_MODEL);
    }

    @Override
    public void render(BuntingBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        for (var e : tile.getBuntings().entrySet()) {
            DyeColor color = e.getValue();
            if (color != null) {
                poseStack.pushPose();

                Direction dir = e.getKey();
                poseStack.mulPose(RotHlpr.rot(dir));
                poseStack.translate(0, 0, 0.5);
                model.render(poseStack, ModMaterials.BUNTING_MATERIAL.buffer(bufferIn, RenderType::entityCutout),
                        combinedLightIn, combinedOverlayIn);
                poseStack.popPose();
            }
        }

    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("bunting", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.0F, -10.0F, 0.0F, 6.0F, 10.0F, 0.0F),
                PartPose.offset(0.0F, 8, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }
}