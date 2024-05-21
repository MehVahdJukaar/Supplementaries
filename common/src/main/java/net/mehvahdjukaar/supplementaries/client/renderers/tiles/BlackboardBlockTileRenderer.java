package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.SuppClientPlatformStuff;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
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
import org.joml.Vector2i;


public class BlackboardBlockTileRenderer implements BlockEntityRenderer<BlackboardBlockTile> {

    public static final int WIDTH = 6;

    private final Minecraft mc;
    private final Camera camera;
    private final boolean noise;

    public BlackboardBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.mc = Minecraft.getInstance();
        this.camera = this.mc.gameRenderer.getMainCamera();
        this.noise = MiscUtils.FESTIVITY.isAprilsFool() && PlatHelper.getPlatform().isForge();
    }

    @Override
    public int getViewDistance() {
        return noise ? 64 : 8;
    }

    @Override
    public boolean shouldRender(BlackboardBlockTile blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos);
    }

    @Override
    public void render(BlackboardBlockTile tile, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light,
                       int combinedOverlayIn) {

        if (!CommonConfigs.Building.BLACKBOARD_MODE.get().canManualDraw() && !noise) return;

        Direction dir = tile.getDirection();
        float yaw = -dir.toYRot();

        Vec3 cameraPos = camera.getPosition();
        BlockPos pos = tile.getBlockPos();


        if (noise) {
            int lu = light & '\uffff';
            int lv = light >> 16 & '\uffff';

            //SuppPlatformStuff.getNoiseShader().getUniform("NoiseScale").set(10000);
            //SuppPlatformStuff.getNoiseShader().getUniform("NoiseSpeed").set(10);
            SuppClientPlatformStuff.getNoiseShader().getUniform("Intensity").set(1.0f);

            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(RotHlpr.rot(dir));
            poseStack.translate(-0.5, -0.5, 0.1875 - 0.001);

            VertexConsumer builder = ModMaterials.BLACKBOARD_OUTLINE.buffer(bufferSource, SuppClientPlatformStuff::staticNoise);


            VertexUtil.addQuad(builder, poseStack, 0, 0, 1, 1,
                    0, 0, 1, 1,
                    255, 255, 255, 255,
                    lu, lv);
            poseStack.popPose();

            return;
        }


        if (LOD.isOutOfFocus(cameraPos, pos, yaw, 0, dir, WIDTH / 16f)) return;

        HitResult hit = mc.hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            if (blockHit.getBlockPos().equals(pos) && tile.getDirection() == blockHit.getDirection()) {
                Player player = mc.player;
                if (player != null && Utils.mayPerformBlockAction(player, pos, player.getMainHandItem())
                        && BlackboardBlock.getStackChalkColor(player.getMainHandItem()) != null) {

                    poseStack.pushPose();
                    poseStack.translate(0.5, 0.5, 0.5);
                    poseStack.mulPose(RotHlpr.rot(dir));
                    poseStack.translate(-0.5, -0.5, 0.1875 - 0.001);

                    int lu = light & '\uffff';
                    int lv = light >> 16 & '\uffff';

                    Vector2i pair = BlackboardBlock.getHitSubPixel(blockHit);
                    float p = 1 / 16f;
                    float x = pair.x() * p;
                    float y = pair.y() * p;

                    VertexConsumer builder = ModMaterials.BLACKBOARD_OUTLINE.buffer(bufferSource, RenderType::entityCutout);

                    poseStack.translate(1 - x - p, 1 - y - p, 0);

                    VertexUtil.addQuad(builder, poseStack, 0, 0, p, p, lu, lv);

                    poseStack.popPose();
                }
            }
        }
    }
}