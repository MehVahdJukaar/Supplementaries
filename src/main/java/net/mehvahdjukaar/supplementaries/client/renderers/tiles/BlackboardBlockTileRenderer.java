package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.supplementaries.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.client.Materials;
import net.mehvahdjukaar.supplementaries.client.renderers.BlackboardTextureManager;
import net.mehvahdjukaar.supplementaries.client.renderers.Const;
import net.mehvahdjukaar.supplementaries.client.renderers.LOD;
import net.mehvahdjukaar.supplementaries.client.renderers.RendererUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;


public class BlackboardBlockTileRenderer extends TileEntityRenderer<BlackboardBlockTile> {

    private final Minecraft MC;

    public BlackboardBlockTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        this.MC = Minecraft.getInstance();
    }

    public final int WIDTH = 6;
    @Override
    public void render(BlackboardBlockTile tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        Direction dir = tile.getDirection();
        float yaw = -dir.toYRot();
        ActiveRenderInfo camera = this.renderer.camera;
        Vector3d cameraPos = camera.getPosition();
        BlockPos pos = tile.getBlockPos();
        if (LOD.isOutOfFocus(cameraPos, pos, yaw, 0, dir, WIDTH / 16f)) return;


        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(Const.rot(dir));
        matrixStackIn.mulPose(Const.XN90);
        matrixStackIn.translate(-0.5, -0.5, -0.1875);



        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';

        RayTraceResult hit = MC.hitResult;
        if (hit != null && hit.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult blockHit = (BlockRayTraceResult) hit;
            if(blockHit.getBlockPos().equals(pos) && tile.getDirection() == blockHit.getDirection()) {
                PlayerEntity player = MC.player;
                if(player != null) {
                    if(BlackboardBlock.getStackChalkColor(player.getMainHandItem()) != null) {
                        Pair<Integer, Integer> pair = BlackboardBlock.getHitSubPixel(blockHit);
                        float p = 1 / 16f;
                        float x = pair.getFirst() * p;
                        float y = pair.getSecond() * p;
                        IVertexBuilder builder2 = Materials.BLACKBOARD_GRID.buffer(bufferIn, RenderType::entityCutout);
                        matrixStackIn.pushPose();

                        matrixStackIn.translate(x,1 - y - p, 0.001);
                        RendererUtil.addQuadSide(builder2, matrixStackIn, 0, 0, 0, p, p, 0, 0, 0, 1, 1, 1, 1, 1, 1, lu, lv, 0, 0, 1, Materials.BLACKBOARD_GRID.sprite());
                        matrixStackIn.popPose();
                    }
                }
            }
        }

        IVertexBuilder builder = bufferIn.getBuffer(BlackboardTextureManager.INSTANCE.getRenderType(tile));
        RendererUtil.addQuadSide(builder, matrixStackIn, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, lu, lv, 0, 0, 1);

        matrixStackIn.popPose();

    }
}