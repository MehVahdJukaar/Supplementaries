package net.mehvahdjukaar.supplementaries.client.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.math.EntityAngles;
import net.mehvahdjukaar.supplementaries.client.ModMaterials;
import net.mehvahdjukaar.supplementaries.client.ModRenderTypes;
import net.mehvahdjukaar.supplementaries.common.block.cannon.BallisticTrajectory;
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
import org.joml.Quaterniondc;
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


        // When the cannon sits on a sublevel (e.g. a Create Aeronautics ship), the incoming pose
        // stack carries the sublevel's tilt. Gravity for the trajectory parabola is world-aligned,
        // so we strip the sublevel transform and render in world space to keep the arrows upright.
        ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(tile);
        Quaternionf cannonOrientation = tile.getLocalOrientation(partialTicks);
        if (subLevel != null) {
            Pose3dc subPose = subLevel.renderPose(partialTicks);
            cannonPos = subPose.transformPosition(cannonPos);
            Quaterniondc q = subPose.orientation();
            Quaternionf subOrient = new Quaternionf(
                    (float) q.x(), (float) q.y(), (float) q.z(), (float) q.w());
            cannonOrientation = subOrient.mul(cannonOrientation, new Quaternionf());
        }

        float yaw = EntityAngles.fromQuaternion(cannonOrientation).yawRad();

        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        if (subLevel != null) resetPoseToWorld(poseStack, cannonPos, camPos);
        //rotate so we can work in 2d. yaw is entity like so its inverted
        poseStack.mulPose(Axis.YP.rotation(-yaw));

        boolean hitAir = shootingMode == ShootingMode.STRAIGHT || trajectory.miss() ||
                mc.level.getBlockState(trajectory.getHitPos(cannonPos, yaw)).isAir();

        renderArrows(poseStack, buffer, partialTicks,
                trajectory, hitAir, rendersRed);

        poseStack.popPose();

        if (!hitAir && hit instanceof BlockHitResult bh) {
            if (bh.getDirection() == Direction.UP) {
                poseStack.pushPose();
                if (subLevel != null) resetPoseToWorld(poseStack, cannonPos, camPos);
                renderTargetCircle(poseStack, buffer, rendersRed, trajectory.getHitLocation(Vec3.ZERO, yaw), partialTicks);
                poseStack.popPose();
            }
        }

        if (!hitAir && debug && hit instanceof BlockHitResult bh) {
            //TODO: multiply by inverse rot
            //poseStack.mulPose(Axis.YP.rotationDegrees(-cannon.getCannonGlobalYawOffset(partialTicks)));
            poseStack.pushPose();
            if (subLevel != null) resetPoseToWorld(poseStack, cannonPos, camPos);
            renderBlockReticule(poseStack, buffer, cannonPos, bh);
            poseStack.popPose();
        }
    }

    // Replace the current pose with a world-space, camera-relative transform at the cannon's
    // world position. The ModelView matrix (in RenderSystem) already carries the camera rotation,
    // so an identity pose here renders straight in world space.
    private static void resetPoseToWorld(PoseStack poseStack, Vec3 cannonPos, Vec3 camPos) {
        poseStack.last().pose().identity();
        poseStack.last().normal().identity();
        poseStack.translate(cannonPos.x - camPos.x, cannonPos.y - camPos.y, cannonPos.z - camPos.z);
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
        finalTime = Math.clamp(finalTime, 1, 100000);

        poseStack.pushPose();

        float scale = 1;
        float size = 2.5f / 16f * scale;
        VertexConsumer consumer = buffer.getBuffer(red ? ModRenderTypes.CANNON_TRAJECTORY_RED : ModRenderTypes.CANNON_TRAJECTORY);
        Matrix4f matrix = poseStack.last().pose();
        //use triangle strips instead?
        float py = 0;
        float px = 0;
        float scrollAmount = -(System.currentTimeMillis() % 1000) / 1000f;
        float step = finalTime / (int) finalTime;
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
