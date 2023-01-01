package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.util.math.Vec2i;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.renderers.VertexUtils;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
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

    public static final int WIDTH = 6;

    private final Minecraft mc;
    private final Camera camera;

    public BlackboardBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.mc = Minecraft.getInstance();
        this.camera = this.mc.gameRenderer.getMainCamera();
    }

    @Override
    public int getViewDistance() {
        return 8;
    }

    @Override
    public boolean shouldRender(BlackboardBlockTile blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos);
    }

    @Override
    public void render(BlackboardBlockTile tile, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        if (!CommonConfigs.Blocks.BLACKBOARD_MODE.get().canManualDraw()) return;

        Direction dir = tile.getDirection();
        float yaw = -dir.toYRot();

        Vec3 cameraPos = camera.getPosition();
        BlockPos pos = tile.getBlockPos();
        if (LOD.isOutOfFocus(cameraPos, pos, yaw, 0, dir, WIDTH / 16f)) return;

        HitResult hit = mc.hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            if (blockHit.getBlockPos().equals(pos) && tile.getDirection() == blockHit.getDirection()) {
                Player player = mc.player;
                if (player != null && player.getAbilities().mayBuild) {
                    if (BlackboardBlock.getStackChalkColor(player.getMainHandItem()) != null) {

                        matrixStackIn.pushPose();
                        matrixStackIn.translate(0.5, 0.5, 0.5);
                        matrixStackIn.mulPose(RotHlpr.rot(dir));
                        matrixStackIn.mulPose(RotHlpr.XN90);
                        matrixStackIn.translate(-0.5, -0.5, -0.1875);

                        int lu = combinedLightIn & '\uffff';
                        int lv = combinedLightIn >> 16 & '\uffff';

                        Vec2i pair = BlackboardBlock.getHitSubPixel(blockHit);
                        float p = 1 / 16f;
                        float x = pair.x() * p;
                        float y = pair.y() * p;
                        VertexConsumer builder2 = ModMaterials.BLACKBOARD_OUTLINE.buffer(bufferIn, RenderType::entityCutout);
                        matrixStackIn.pushPose();

                        matrixStackIn.translate(x, 1 - y - p, 0.001);
                        VertexUtils.addQuadSide(builder2, matrixStackIn, 0, 0, 0, p, p, 0, 0, 0, 1, 1, 1, 1, 1, 1, lu, lv, 0, 0, 1, ModMaterials.BLACKBOARD_OUTLINE.sprite());
                        matrixStackIn.popPose();

                        matrixStackIn.popPose();
                    }
                }
            }
        }
    }
}