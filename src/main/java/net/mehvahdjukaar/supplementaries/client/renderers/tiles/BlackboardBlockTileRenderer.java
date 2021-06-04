package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.LOD;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;


public class BlackboardBlockTileRenderer extends TileEntityRenderer<BlackboardBlockTile> {

    public BlackboardBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(BlackboardBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        Direction dir = tile.getDirection();
        float yaw = -dir.toYRot();
        Vector3d cameraPos = this.renderer.camera.getPosition();
        BlockPos pos = tile.getBlockPos();
        if(LOD.isOutOfFocus(cameraPos, pos, yaw, dir, 6/16f))return;

        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5,0.5,0.5);
        matrixStackIn.mulPose(dir.getRotation());
        matrixStackIn.mulPose(Const.XN90);
        matrixStackIn.translate(-0.5,-0.5,-0.1875);

        IVertexBuilder builder = bufferIn.getBuffer(BlackboardTextureManager.INSTANCE.getRenderType(tile));

        RendererUtil.addQuadSide(builder, matrixStackIn, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, lu, lv, 0, 0, 1);

        matrixStackIn.popPose();

    }
}