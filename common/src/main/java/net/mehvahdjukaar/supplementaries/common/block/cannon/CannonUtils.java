package net.mehvahdjukaar.supplementaries.common.block.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.IBallisticBehavior;
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


    public static Pair< @Nullable CannonTrajectory, Float> computeTrajectory(
            CannonAccess access,
            Vec3 targetPos, ShootingMode shootingMode) {
        CannonBlockTile cannonTile = access.getInternalCannon();
        Vec3 cannonPos = access.getCannonGlobalPosition(0);

        Vec3 localTarget = targetPos.subtract(cannonPos);
        //so we hopefully hit the block we are looking at
        localTarget = localTarget.add(localTarget.normalize().scale(0.05f));

        CannonAccess.Restraint restraints = access.getPitchAndYawRestrains();
        IBallisticBehavior.Data ballistic = cannonTile.getTrajectoryData();
        float minPitch = restraints.minPitch() * Mth.DEG_TO_RAD;
        float maxPitch = restraints.maxPitch() * Mth.DEG_TO_RAD;

        var vec3ToPoint = vec3ToPoint2d(localTarget);
        Vec2 targetPoint = vec3ToPoint.getFirst();
        float initialYaw = vec3ToPoint.getSecond();

        CannonTrajectory trajectory = CannonTrajectory.findBest(targetPoint,

                ballistic.gravity(), ballistic.drag(),
                cannonTile.getFirePower() * ballistic.initialSpeed(),
                shootingMode,
                minPitch, maxPitch);
        return Pair.of(trajectory, initialYaw);
    }


    public static Vec3 point2dToVec3(Vec2 point, float yaw) {
        return new Vec3(0, point.y, -point.x).yRot(-yaw);
    }

    public static Pair<Vec2, Float> vec3ToPoint2d(Vec3 point) {
        float yaw = Mth.PI + (float) Mth.atan2(-point.x, point.z);
        Vec2 vec2 = new Vec2((float) Mth.length(point.x, point.z), (float) point.y);
        return Pair.of(vec2, yaw);
    }

    public static void spawnSmokeTrail(Level level, PoseStack poseStack, RandomSource ran, Vec3 sp) {
        int smokeCount = 40;
        for (int i = 0; i < smokeCount; i += 1) {

            poseStack.pushPose();

            Vector4f speed = poseStack.last().pose().transform(new Vector4f(0, 0,
                    -MthUtils.nextWeighted(ran, 0.5f, 1, 0.06f), 0));

            float aperture = 0.5f;
            poseStack.translate(-aperture / 2 + ran.nextFloat() * aperture, -aperture / 2 + ran.nextFloat() * aperture, 0);

            Vector4f p = poseStack.last().pose().transform(new Vector4f(0, 0, 1, 1));

            level.addParticle(ParticleTypes.SMOKE,
                    p.x, p.y, p.z,
                    speed.x + sp.x, speed.y + sp.y, speed.z + sp.z);
            poseStack.popPose();
        }
    }

    public static void spawnDustRing(Level level, PoseStack poseStack, Vec3 sp) {
        poseStack.pushPose();

        Vector4f p = poseStack.last().pose().transform(new Vector4f(0, 0, 1, 1));

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
