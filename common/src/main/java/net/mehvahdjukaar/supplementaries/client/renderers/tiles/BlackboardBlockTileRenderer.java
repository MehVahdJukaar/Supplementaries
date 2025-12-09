package net.mehvahdjukaar.supplementaries.client.renderers.tiles;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.renderers.NoiseRenderType;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ClientRegistry;
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
    private final boolean noise;

    public BlackboardBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.mc = Minecraft.getInstance();
        this.noise = MiscUtils.FESTIVITY.isAprilsFool();
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
        BlockPos pos = tile.getBlockPos();

        LOD lod = LOD.at(tile);

        if (lod.isPlaneCulled(dir, WIDTH / 16f)) return;

        if (noise) {

            int lu = VertexUtil.lightU(light);
            int lv = VertexUtil.lightV(light);

            //SuppPlatformStuff.getNoiseShader().getUniform("NoiseScale").set(10000);
            //SuppPlatformStuff.getNoiseShader().getUniform("NoiseSpeed").set(10);
            Uniform intensity = ClientRegistry.NOISE_SHADER.get().getUniform("Intensity");
            if (intensity != null) intensity.set(1.0f);

            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(RotHlpr.rot(dir));
            poseStack.translate(-0.5, -0.5, 0.1875 - 0.001);

            VertexConsumer builder = ModMaterials.BLACKBOARD_OUTLINE.buffer(bufferSource, NoiseRenderType.RENDER_TYPE);


            VertexUtil.addQuad(builder, poseStack, 0, 0, 1, 1,
                    0, 0, 1, 1,
                    255, 255, 255, 255,
                    lu, lv);
            poseStack.popPose();

            return;
        }

        HitResult hit = mc.hitResult;
        Player player = mc.player;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK && player != null && !player.isSecondaryUseActive()) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            if (blockHit.getBlockPos().equals(pos) && tile.getDirection() == blockHit.getDirection()) {
                if (Utils.mayPerformBlockAction(player, pos, player.getMainHandItem())
                        && BlackboardBlock.getStackChalkColor(player.getMainHandItem()) != null) {

                    poseStack.pushPose();
                    poseStack.translate(0.5, 0.5, 0.5);
                    poseStack.mulPose(RotHlpr.rot(dir));
                    poseStack.translate(-0.5, -0.5, 0.1875 - 0.001);

                    int lu = VertexUtil.lightU(light);
                    int lv = VertexUtil.lightV(light);

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