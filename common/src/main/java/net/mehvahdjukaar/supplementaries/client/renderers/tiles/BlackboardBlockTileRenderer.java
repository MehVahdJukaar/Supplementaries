package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.renderers.VertexUtils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


public class BlackboardBlockTileRenderer implements BlockEntityRenderer<BlackboardBlockTile> {

    public final int WIDTH = 6;

    private final Minecraft MC;
    private final Camera camera;

    public BlackboardBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.MC = Minecraft.getInstance();
        this.camera = this.MC.gameRenderer.getMainCamera();
    }

    @Override
    public int getViewDistance() {
        return 8;
    }

    @Override
    public boolean shouldRender(BlackboardBlockTile p_173568_, Vec3 p_173569_) {
        return BlockEntityRenderer.super.shouldRender(p_173568_, p_173569_);
    }

    @Override
    public void render(BlackboardBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {

        Direction dir = tile.getDirection();
        float yaw = -dir.toYRot();

        Vec3 cameraPos = camera.getPosition();
        BlockPos pos = tile.getBlockPos();
        if (LOD.isOutOfFocus(cameraPos, pos, yaw, 0, dir, WIDTH / 16f)) return;

        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5, 0.5, 0.5);
        matrixStackIn.mulPose(RotHlpr.rot(dir));
        matrixStackIn.mulPose(RotHlpr.XN90);
        matrixStackIn.translate(-0.5, -0.5, -0.1875);


        int lu = combinedLightIn & '\uffff';
        int lv = combinedLightIn >> 16 & '\uffff';

        HitResult hit = MC.hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            if(blockHit.getBlockPos().equals(pos) && tile.getDirection() == blockHit.getDirection()) {
                Player player = MC.player;
                if(player != null) {
                    if(BlackboardBlock.getStackChalkColor(player.getMainHandItem()) != null) {
                        Pair<Integer, Integer> pair = BlackboardBlock.getHitSubPixel(blockHit);
                        float p = 1 / 16f;
                        float x = pair.getFirst() * p;
                        float y = pair.getSecond() * p;
                        VertexConsumer builder2 = ModMaterials.BLACKBOARD_OUTLINE.buffer(bufferIn, RenderType::entityCutout);
                        matrixStackIn.pushPose();

                        matrixStackIn.translate(x,1 - y - p, 0.001);
                        VertexUtils.addQuadSide(builder2, matrixStackIn, 0, 0, 0, p, p, 0, 0, 0, 1, 1, 1, 1, 1, 1, lu, lv, 0, 0, 1, ModMaterials.BLACKBOARD_OUTLINE.sprite());
                        matrixStackIn.popPose();
                    }
                }
            }
        }

       // VertexConsumer builder = bufferIn.getBuffer(BlackboardTextureManager.INSTANCE.getBlackboardInstance(tile).getRenderType());
       // RendererUtil.addQuadSide(builder, matrixStackIn, 0, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, lu, lv, 0, 0, 1);

        matrixStackIn.popPose();

    }
}