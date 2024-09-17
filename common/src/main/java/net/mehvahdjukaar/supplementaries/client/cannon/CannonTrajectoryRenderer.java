package net.mehvahdjukaar.supplementaries.client.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.ModRenderTypes;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static net.mehvahdjukaar.supplementaries.client.cannon.CannonController.*;

public class CannonTrajectoryRenderer {


    public static void render(CannonBlockTile blockEntity, PoseStack poseStack, MultiBufferSource buffer,
                              int packedLight, int packedOverlay, float partialTicks) {
        if (cannon != blockEntity) return;
        if (hit == null || trajectory == null || !showsTrajectory) return;

        boolean rendersRed = !blockEntity.readyToFire();

        BlockPos cannonPos = blockEntity.getBlockPos();


        Minecraft mc = Minecraft.getInstance();
        boolean debug = !mc.showOnlyReducedInfo() && mc.getEntityRenderDispatcher().shouldRenderHitBoxes();


        poseStack.pushPose();

        float yaw = blockEntity.getYaw(partialTicks) * Mth.DEG_TO_RAD;

        //rotate so we can work in 2d
        Vec3 targetVector = hit.getLocation().subtract(cannonPos.getCenter());
        Vec2 target = new Vec2((float) Mth.length(targetVector.x, targetVector.z), (float) targetVector.y);

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotation(-yaw));

        if (debug) renderTargetLine(poseStack, buffer, target);

        boolean hitAir = shootingMode == ShootingMode.STRAIGHT ||
                mc.level.getBlockState(trajectory.getHitPos(cannonPos, yaw)).isAir();

        renderArrows(poseStack, buffer, partialTicks,
                trajectory, hitAir, rendersRed);

        poseStack.popPose();


        if (!hitAir) renderTargetCircle(poseStack, buffer, yaw, rendersRed);

        if (!hitAir && debug && hit instanceof BlockHitResult bh)
            renderTargetBlock(poseStack, buffer, cannonPos, bh);
    }

    private static void renderTargetBlock(PoseStack poseStack, MultiBufferSource buffer, BlockPos pos, BlockHitResult bh) {
        poseStack.pushPose();

        BlockPos targetPos = bh.getBlockPos();
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        Vec3 distance1 = targetPos.getCenter().subtract(pos.getCenter());

        AABB bb = new AABB(distance1, distance1.add(1, 1, 1)).inflate(0.01);
        LevelRenderer.renderLineBox(poseStack, lines, bb, 1.0F, 0, 0, 1.0F);

        poseStack.popPose();
    }

    private static void renderTargetCircle(PoseStack poseStack, MultiBufferSource buffer, float yaw, boolean red) {
        poseStack.pushPose();

        Material circleMaterial = red ? ModMaterials.CANNON_TARGET_RED_MATERIAL : ModMaterials.CANNON_TARGET_MATERIAL;
        VertexConsumer circleBuilder = circleMaterial.buffer(buffer, RenderType::entityCutout);

        Vec3 targetVec = new Vec3(0, trajectory.point().y, -trajectory.point().x).yRot(-yaw);
        poseStack.translate(targetVec.x + 0.5, targetVec.y + 0.5 + 0.05, targetVec.z + 0.5);

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
        consumer.addVertex(pose, 0, target.y, -target.x).setColor(255, 0, 0, 255).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, 0.01f, target.y, -target.x).setColor(255, 0, 0, 255).setNormal(pose, 0.0F, 0.0F, 1.0F);
        consumer.addVertex(pose, 0.01f, 0, 0).setColor(255, 0, 0, 255).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }


    private static void renderArrows(PoseStack poseStack, MultiBufferSource buffer, float partialTicks,
                                     CannonTrajectory trajectory, boolean hitAir, boolean red) {

        float finalTime = (float) trajectory.finalTime();
        if (finalTime > 100000) {
            Supplementaries.error();
            return;
        }

        poseStack.pushPose();

        float scale = 1;
        float size = 2.5f / 16f * scale;
        VertexConsumer consumer = buffer.getBuffer(red ? ModRenderTypes.CANNON_TRAJECTORY_RED : ModRenderTypes.CANNON_TRAJECTORY);
        Matrix4f matrix = poseStack.last().pose();

        float py = 0;
        float px = 0;
        float d = -(System.currentTimeMillis() % 1000) / 1000f;
        float step = finalTime / (int) finalTime;
        float maxT = finalTime + (hitAir ? 0 : step);
        for (float t = step; t < maxT; t += step) {

            float textureStart = d % 1;
            consumer.addVertex(matrix, -size, py, px)
                    .setColor(1, 1, 1, 1.0F)
                    .setUv(0, textureStart);
            consumer.addVertex(matrix, size, py, px)
                    .setColor(1, 1, 1, 1.0F)
                    .setUv(5 / 16f, textureStart);

            double ny = trajectory.getY(t);
            double nx = -trajectory.getX(t);

            float dis = (float) (Mth.length(nx - px, ny - py)) / scale;
            float textEnd = textureStart + dis;

            d += dis;
            py = (float) ny;
            px = (float) nx;

            int alpha = (t + step >= maxT) ? 0 : 1;
            consumer.addVertex(matrix, size, py, px)
                    .setColor(1f, 1f, 1f, alpha)
                    .setUv(5 / 16f, textEnd);
            consumer.addVertex(matrix, -size, py, px)
                    .setColor(1f, 1f, 1f, alpha)
                    .setUv(0, textEnd);
        }

        poseStack.popPose();
    }
}
