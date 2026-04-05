package net.mehvahdjukaar.supplementaries.common.block.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.BallisticData;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

public class CannonUtils {

    public static BallisticTrajectory3D computeTrajectory(
            CannonBlockTile cannon, Vec3 targetPos, ShootingMode shootingMode) {
        Vec3 cannonPos = cannon.getGlobalPosition(0);

        Vec3 localTarget = targetPos.subtract(cannonPos);
        //so we hopefully hit the block we are looking at
        localTarget = localTarget.add(localTarget.normalize().scale(0.05f));

        YawPitchRestraint restraints = cannon.getOrientationRestraints();
        BallisticData ballistic = cannon.getTrajectoryData();
        float minPitch = restraints.minPitchDeg() * Mth.DEG_TO_RAD;
        float maxPitch = restraints.maxPitchDeg() * Mth.DEG_TO_RAD;

        var vec3ToPoint = vec3ToPoint2d(localTarget);

        BallisticTrajectory trajectory = BallisticTrajectory.findBest(vec3ToPoint.target,
                ballistic.gravity(), ballistic.drag(),
                cannon.getFirePower() * ballistic.initialSpeed(),
                shootingMode,
                minPitch, maxPitch);
        return trajectory == null ? null : new BallisticTrajectory3D(trajectory, vec3ToPoint.yaw);
    }

    private record Target2dAndYaw(Vec2 target, float yaw) {
    }

    public static Vec3 point2dToVec3(Vec2 point, float yaw) {
        return new Vec3(0, point.y, point.x).yRot(-yaw);
    }

    private static Target2dAndYaw vec3ToPoint2d(Vec3 point) {
        float yaw = (float) Mth.atan2(-point.x, point.z);
        Vec2 vec2 = new Vec2((float) Mth.length(point.x, point.z), (float) point.y);
        return new Target2dAndYaw(vec2, yaw);
    }

    public static void spawnSmokeTrail(Level level, PoseStack poseStack, RandomSource ran, Vec3 sp) {
        int smokeCount = 40;
        for (int i = 0; i < smokeCount; i += 1) {

            poseStack.pushPose();

            Vector4f speed = poseStack.last().pose().transform(new Vector4f(0, 0,
                    MthUtils.nextWeighted(ran, 0.5f, 1, 0.06f), 0));

            float aperture = 0.5f;
            poseStack.translate(-aperture / 2 + ran.nextFloat() * aperture, -aperture / 2 + ran.nextFloat() * aperture, 0);

            Vector4f p = poseStack.last().pose().transform(new Vector4f());

            level.addParticle(ParticleTypes.SMOKE,
                    p.x, p.y, p.z,
                    speed.x + sp.x, speed.y + sp.y, speed.z + sp.z);
            poseStack.popPose();
        }
    }

    public static void spawnDustRing(Level level, PoseStack poseStack, Vec3 sp) {
        poseStack.pushPose();

        Vector4f p = poseStack.last().pose().transform(new Vector4f());

        int dustCount = 16;
        for (int i = 0; i < dustCount; i += 1) {

            poseStack.pushPose();

            poseStack.mulPose(Axis.YP.rotationDegrees(90));

            poseStack.mulPose(Axis.XP.rotationDegrees(380f * i / dustCount));
            float vel = 0.05f;

            Vector4f speed = poseStack.last().pose().transform(new Vector4f(0, 0, vel, 0));
            SimpleParticleType campfireCosySmoke = ModParticles.BOMB_SMOKE_PARTICLE.get();

            level.addParticle(campfireCosySmoke,
                    p.x, p.y, p.z,
                    speed.x + sp.x, speed.y + sp.y, speed.z + sp.z);
            poseStack.popPose();
        }

        poseStack.popPose();
    }


}
