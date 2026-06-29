package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.common.block.tiles.WindVaneBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;


public class WindVaneBlockTileRenderer implements BlockEntityRenderer<WindVaneBlockTile> {

    private final ModelPart model;

    public WindVaneBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.model = context.bakeLayer(ClientRegistry.WIND_VANE_MODEL);
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

    @Override
    public void render(WindVaneBlockTile tile, float partialTicks, PoseStack ps, MultiBufferSource buffers,
                       int light, int overlay) {

        ps.pushPose();
        ps.translate(0.5, 1.0, 0.5);

        ps.translate(-1.5, -0.5, 0.2);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.GLOWSTONE.defaultBlockState(), ps, buffers, light, overlay);

        ps.popPose();
    }

}
