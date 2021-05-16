package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.mehvahdjukaar.supplementaries.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.LOD;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;


public class BlackboardBlockTileRenderer extends TileEntityRenderer<BlackboardBlockTile> {

    RenderType BLACKBOARD_RENDER_TYPE = RenderType.entitySolid(Textures.BLACKBOARD_TEXTURE);

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

        IVertexBuilder builder = bufferIn.getBuffer(BLACKBOARD_RENDER_TYPE);

        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5,0.5,0.5);

        matrixStackIn.mulPose(dir.getOpposite().getRotation());
        matrixStackIn.mulPose(Const.XN90);
        matrixStackIn.translate(0.5,0.5,0.1875);
        matrixStackIn.scale(-1,-1,1);

        float w = 1/16f;
        for (int x=0; x < tile.pixels.length; x++) {
            for (int y = 0; y < tile.pixels[x].length; y++) {

                float x0 = x * w;
                float x1 = (x + 1) * w;
                float y0 = y * w;
                float y1 = (y + 1) * w;
                float offset = tile.pixels[x][y] > 0?0.5f:0;

                int rgb = BlackboardBlock.colorFromByte(tile.pixels[x][y]);
                float b = NativeImage.getR(rgb)/255f;
                float g = NativeImage.getG(rgb)/255f;
                float r = NativeImage.getB(rgb)/255f;

                RendererUtil.addQuadSide(builder, matrixStackIn, x1, y0, 0, x0, y1, 0, offset + x0/2f, y0, offset + x1/2f, y1, r, g, b, 1, lu, lv, 0, 0, 1);

            }
        }
        matrixStackIn.popPose();

    }
}