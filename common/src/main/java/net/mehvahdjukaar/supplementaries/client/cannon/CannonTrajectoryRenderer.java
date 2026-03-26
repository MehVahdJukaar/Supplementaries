package net.mehvahdjukaar.supplementaries.client.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.ModRenderTypes;
import net.mehvahdjukaar.supplementaries.common.block.cannon.BallisticTrajectory;
import net.mehvahdjukaar.supplementaries.common.block.cannon.EulerAngles;
import net.mehvahdjukaar.supplementaries.common.block.cannon.ShootingMode;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import static net.mehvahdjukaar.supplementaries.client.cannon.CannonController.*;

public class CannonTrajectoryRenderer {


    public static void render(CannonBlockTile tile, PoseStack poseStack, MultiBufferSource buffer,
                              int packedLight, int packedOverlay, float partialTicks) {
        if (cannon != tile) return;
        if (hit == null || trajectory == null || !showsTrajectory) return;

        boolean rendersRed = !tile.readyToFire();

        Vec3 cannonPos = cannon.getGlobalPosition(partialTicks);


        Minecraft mc = Minecraft.getInstance();
        boolean debug = PlatHelper.isDev() || !mc.showOnlyReducedInfo() && mc.getEntityRenderDispatcher().shouldRenderHitBoxes();


        poseStack.pushPose();

        Quaternionf rot = tile.getWorldOrientation(partialTicks);
        EulerAngles eulerAngles = EulerAngles.fromRotation(rot);

        float yaw = (eulerAngles.yaw()) * Mth.DEG_TO_RAD;

        //rotate so we can work in 2d

        poseStack.mulPose(Axis.YP.rotation(-yaw));

        boolean hitAir = shootingMode == ShootingMode.STRAIGHT || trajectory.miss() ||
                mc.level.getBlockState(trajectory.getHitPos(cannonPos, yaw)).isAir();

        renderArrows(poseStack, buffer, partialTicks,
                trajectory, hitAir, rendersRed);

        poseStack.popPose();

        if (!hitAir && hit instanceof BlockHitResult bh) {
            if (bh.getDirection() == Direction.UP) {
                renderTargetCircle(poseStack, buffer, rendersRed, trajectory.getHitLocation(Vec3.ZERO, yaw), partialTicks);
            }
        }

        if (!hitAir && debug && hit instanceof BlockHitResult bh) {
            //TODO: multiply by inverse rot
            //poseStack.mulPose(Axis.YP.rotationDegrees(-cannon.getCannonGlobalYawOffset(partialTicks)));
            renderBlockReticule(poseStack, buffer, cannonPos, bh);
        }
    }

    private static void renderBlockReticule(PoseStack poseStack, MultiBufferSource buffer,
                                            Vec3 pos, BlockHitResult bh) {
        poseStack.pushPose();

        BlockPos targetPos = bh.getBlockPos();
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        Vec3 relative = new Vec3(targetPos.getX(), targetPos.getY(), targetPos.getZ()).subtract(pos);

        AABB bb = new AABB(relative, relative.add(1, 1, 1)).inflate(0.01);
        LevelRenderer.renderLineBox(poseStack, lines, bb, 1.0F, 0, 0, 1.0F);

        poseStack.popPose();
    }

    private static void renderTargetCircle(PoseStack poseStack, MultiBufferSource buffer,
                                           boolean red, Vec3 targetPos, float partialTicks) {
        poseStack.pushPose();

        Material circleMaterial = red ? ModMaterials.CANNON_TARGET_RED_MATERIAL : ModMaterials.CANNON_TARGET_MATERIAL;
        VertexConsumer circleBuilder = circleMaterial.buffer(buffer, RenderType::entityCutout);

        poseStack.translate(targetPos.x, targetPos.y + 0.05, targetPos.z);

        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        int lu = LightTexture.FULL_BLOCK;
        int lv = LightTexture.FULL_SKY;
        VertexUtil.addQuad(circleBuilder, poseStack, -2f, -2f, 2f, 2f, lu, lv);
        poseStack.popPose();
    }

    private static void renderTargetLine(PoseStack poseStack, MultiBufferSource buffer, Vec2 target) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
        var pose = poseStack.last();
        consumer.addVertex(pose, 0, 0, 0).setColor(255, 0, 0, 255).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, 0, target.y, target.x).setColor(255, 0, 0, 255).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, 0.01f, target.y, target.x).setColor(255, 0, 0, 255).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, 0.01f, 0, 0).setColor(255, 0, 0, 255).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }


    private static void renderArrows(PoseStack poseStack, MultiBufferSource buffer, float partialTicks,
                                     BallisticTrajectory trajectory, boolean hitAir, boolean red) {

        float finalTime = (float) trajectory.finalTime();
        finalTime = Math.clamp( finalTime, 1, 100000);

        poseStack.pushPose();

        float scale = 1;
        float size = 2.5f / 16f * scale;
        VertexConsumer consumer = buffer.getBuffer(red ? ModRenderTypes.CANNON_TRAJECTORY_RED : ModRenderTypes.CANNON_TRAJECTORY);
        Matrix4f matrix = poseStack.last().pose();
        //use triangle strips instead?
        float py = 0;
        float px = 0;
        float scrollAmount = -(System.currentTimeMillis() % 1000) / 1000f;
        float step =  finalTime / (int) finalTime;
        float maxT = finalTime + (hitAir ? 0 : step);
        for (float segmentTime = step; segmentTime < maxT; segmentTime += step) {

            float textureStart = scrollAmount % 1;
            consumer.addVertex(matrix, -size, py, px)
                    .setColor(1, 1, 1, 1.0F)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setUv(0, textureStart);
            consumer.addVertex(matrix, size, py, px)
                    .setColor(1, 1, 1, 1.0F)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setUv(5 / 16f, textureStart);

            double ny = trajectory.getY(segmentTime);
            double nx = trajectory.getX(segmentTime);

            float dis = (float) (Mth.length(nx - px, ny - py)) / scale;
            float textEnd = textureStart + dis;

            scrollAmount += dis;
            py = (float) ny;
            px = (float) nx;

            int alpha = (segmentTime + step >= maxT) ? 0 : 1;
            consumer.addVertex(matrix, size, py, px)
                    .setColor(1, 1, 1f, alpha)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setUv(5 / 16f, textEnd);
            consumer.addVertex(matrix, -size, py, px)
                    .setColor(1, 1, 1f, alpha)
                    .setLight(LightTexture.FULL_BRIGHT)
                    .setUv(0, textEnd);
        }

        poseStack.popPose();
    }
}
