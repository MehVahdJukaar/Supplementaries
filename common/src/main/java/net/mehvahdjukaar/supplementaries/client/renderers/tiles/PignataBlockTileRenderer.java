package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.tiles.PignataBlockTile;
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

public class PignataBlockTileRenderer implements BlockEntityRenderer<PignataBlockTile> {

    private final ModelPart pig;
    private final ModelPart hook;

    public PignataBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(ClientRegistry.PIGNATA_MODEL);
        this.pig = root.getChild("bone");
        this.hook = root.getChild("bb_main");
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-14.0F, -16.0F, 3.0F, 12.0F, 13.0F, 13.0F)
                        .texOffs(0, 26).addBox(-12.0F, -13.0F, 0.0F, 8.0F, 7.0F, 5.0F)
                        .texOffs(0, 38).addBox(-7.0F, -3.0F, 4.0F, 4.0F, 3.0F, 4.0F)
                        .texOffs(0, 38).addBox(-13.0F, -3.0F, 4.0F, 4.0F, 3.0F, 4.0F)
                        .texOffs(0, 38).addBox(-7.0F, -3.0F, 11.0F, 4.0F, 3.0F, 4.0F)
                        .texOffs(0, 38).addBox(-13.0F, -3.0F, 11.0F, 4.0F, 3.0F, 4.0F),
                PartPose.offset(8.0F, 24.0F, -8.0F));

        partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create()
                        .texOffs(27, 34).addBox(-2.0F, -10.0F, -9.0F, 4.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void render(PignataBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn,
                       int combinedLightIn, int combinedOverlayIn) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.scale(-1, -1, 1);
        var builder = ModMaterials.PIGNATA_MATERIAL.buffer(bufferIn, RenderType::entityCutout);
        pig.render(poseStack, builder, combinedLightIn, combinedOverlayIn);
        hook.render(poseStack, builder, combinedLightIn, combinedOverlayIn);

        poseStack.popPose();
    }
}
