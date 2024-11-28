package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BellowsBlockTile;
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
import net.minecraft.world.phys.AABB;


public class BellowsBlockTileRenderer implements BlockEntityRenderer<BellowsBlockTile> {
    private final ModelPart center;
    private final ModelPart top;
    private final ModelPart leather;

    public static LayerDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("center", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-2.0F, -2.0F, -8.0F, 4.0F, 1.0F, 1.0F)
                .texOffs(0, 2)
                .addBox(-2.0F, 1.0F, -8.0F, 4.0F, 1.0F, 1.0F)
                .texOffs(0, 19)
                .addBox(-8.0F, -1.0F, -8.0F, 16.0F, 2.0F, 16.0F),
                PartPose.ZERO);

        root.addOrReplaceChild("top", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-8.0F, 5.0F, -8.0F, 16.0F, 3.0F, 16.0F),
                PartPose.ZERO);

        root.addOrReplaceChild("leather", CubeListBuilder.create()
                .texOffs(0, 37)
                .addBox(-7.0F, -5.0F, -7.0F, 14.0F, 10.0F, 14.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    public BellowsBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart model = context.bakeLayer(ClientRegistry.BELLOWS_MODEL);
        this.center = model.getChild("center");
        this.leather = model.getChild("leather");
        this.top = model.getChild("top");
    }

    @ForgeOverride
    public AABB getRenderBoundingBox(BellowsBlockTile tile) {
        return new AABB(tile.getBlockPos());
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public void render(BellowsBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        VertexConsumer builder = ModMaterials.BELLOWS_MATERIAL.buffer(bufferIn, RenderType::entitySolid);


        float dh = tile.getHeight(partialTicks);

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        Direction dir = tile.getDirection();
        poseStack.mulPose(RotHlpr.rot(dir));
        poseStack.scale(-1,-1,1);

        center.render(poseStack, builder, combinedLightIn, combinedOverlayIn);


        poseStack.pushPose();

        poseStack.translate(0, -1+(3/16d)-dh, 0);

        top.render(poseStack, builder, combinedLightIn, combinedOverlayIn);

        poseStack.popPose();

        poseStack.pushPose();

        poseStack.translate(0, dh,0);

        top.render(poseStack, builder, combinedLightIn, combinedOverlayIn);

        poseStack.popPose();

        float j = 3.2f;

        poseStack.scale(1, 1+j*dh, 1);

        leather.render(poseStack, builder, combinedLightIn, combinedOverlayIn);

        poseStack.popPose();
    }
}