package net.mehvahdjukaar.supplementaries.client.renderers.tiles;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.supplementaries.block.tiles.BellowsBlockTile;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;


public class BellowsBlockTileRenderer implements BlockEntityRenderer<BellowsBlockTile> {
    private final ModelPart center = new ModelPart(64, 64, 0, 0);
    private final ModelPart top = new ModelPart(64, 64, 0, 0);
    private final ModelPart leather = new ModelPart(64, 64, 0, 0);

    public BellowsBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        center.setPos(0.0F, 0.0F, 0.0F);
        center.texOffs(0, 0).addBox(-2.0F, -2.0F, -8.0F, 4.0F, 1.0F, 1.0F, 0.0F, false);
        center.texOffs(0, 2).addBox(-2.0F, 1.0F, -8.0F, 4.0F, 1.0F, 1.0F, 0.0F, false);
        center.texOffs(0, 19).addBox(-8.0F, -1.0F, -8.0F, 16.0F, 2.0F, 16.0F, 0.0F, false);

        top.setPos(0.0F, 0.0F, 0.0F);
        //top.setTextureOffset(0, 0).addBox(-8.0F, -8.0F, -8.0F, 16.0F, 3.0F, 16.0F, 0.0F, false);
        top.texOffs(0, 0).addBox(-8.0F, 5.0F, -8.0F, 16.0F, 3.0F, 16.0F, 0.0F, false);

        leather.setPos(0.0F, 0.0F, 0.0F);
        leather.texOffs(0, 37).addBox(-7.0F, -5.0F, -7.0F, 14.0F, 10.0F, 14.0F, 0.0F, false);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public void render(BellowsBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        VertexConsumer builder = Materials.BELLOWS_MATERIAL.buffer(bufferIn, RenderType::entitySolid);


        float dh = Mth.lerp(partialTicks, tile.prevHeight, tile.height);

        matrixStackIn.pushPose();

        matrixStackIn.translate(0.5, 0.5, 0.5);

        Direction dir = tile.getDirection();
        matrixStackIn.mulPose(Const.rot(dir.getOpposite()));
        matrixStackIn.mulPose(Const.XN90);
        matrixStackIn.mulPose(Const.Z180);
        //TODO: figure out why models are always flipped

        center.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);


        matrixStackIn.pushPose();
        //TODO: maybe render bottom instead
        matrixStackIn.translate(0, -1+(3/16d)-dh, 0);

        top.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.popPose();

        matrixStackIn.pushPose();

        matrixStackIn.translate(0, dh,0);

        top.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.popPose();

        float j = 3.2f;

        matrixStackIn.scale(1, 1+j*dh, 1);

        leather.render(matrixStackIn, builder, combinedLightIn, combinedOverlayIn);

        matrixStackIn.popPose();
    }
}