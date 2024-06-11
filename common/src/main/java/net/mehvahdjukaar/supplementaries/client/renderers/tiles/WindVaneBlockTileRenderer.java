package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WindVaneBlockTile;
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
import net.minecraft.util.Mth;


public class WindVaneBlockTileRenderer implements BlockEntityRenderer<WindVaneBlockTile> {

    private final ModelPart model;

    public WindVaneBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.model = context.bakeLayer(ClientRegistry.WIND_VANE_MODEL);
    }

    @Override
    public void render(WindVaneBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.scale(1,-1,-1);

        model.yRot = Mth.DEG_TO_RAD * tile.getYaw(partialTicks);
        model.render(matrixStackIn, ModMaterials.WIND_VANE_MATERIAL.buffer(bufferIn, RenderType::entityCutout),
                combinedLightIn, combinedOverlayIn);
        //RenderUtil.renderModel(ClientRegistry.WIND_VANE_MODEL, matrixStackIn, bufferIn, blockRenderer,
        //         combinedLightIn, combinedOverlayIn, true);

        matrixStackIn.popPose();

    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("chicken",
                CubeListBuilder.create().texOffs(0, -11)
                        .addBox(0.0F, -8.0F, -5.5F, 0.0F, 11.0F, 11.0F),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 32, 32);
    }
}