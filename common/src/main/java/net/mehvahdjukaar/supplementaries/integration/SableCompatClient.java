package net.mehvahdjukaar.supplementaries.integration;

import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;

public class SableCompatClient {

    public record WorldPose(Vec3 pos, Quaternionf orientation, boolean onSubLevel) {
    }

    public static WorldPose projectOutOfSubLevel(BlockEntity be, Vec3 pos, Quaternionf orientation,
                                                 float partialTicks) {
        ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(be);
        if (subLevel == null) return new WorldPose(pos, orientation, false);
        Pose3dc subPose = subLevel.renderPose(partialTicks);
        Quaterniondc q = subPose.orientation();
        Quaternionf subOrientation = new Quaternionf((float) q.x(), (float) q.y(), (float) q.z(), (float) q.w());
        return new WorldPose(subPose.transformPosition(pos),
                subOrientation.mul(orientation, new Quaternionf()), true);
    }

    public static Vec3 projectOutOfSubLevel(BlockEntity be, Vec3 pos, float partialTicks) {
        ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(be);
        return subLevel == null ? pos : subLevel.renderPose(partialTicks).transformPosition(pos);
    }

    public static Vec3 projectIntoSubLevel(BlockEntity be, Vec3 worldPos, float partialTicks) {
        ClientSubLevelAccess subLevel = SableCompanion.INSTANCE.getContainingClient(be);
        return subLevel == null ? worldPos : subLevel.renderPose(partialTicks).transformPositionInverse(worldPos);
    }

    public static HitResult clipIncludingSubLevels(BlockGetter level, Entity entity, Vec3 start, Vec3 end,
                                                   float partialTicks) {
        SableCompanion sable = SableCompanion.INSTANCE;
        ClientSubLevelAccess originSubLevel = sable.getContainingClient(start);
        Pose3dc originPose = originSubLevel == null ? null : originSubLevel.renderPose(partialTicks);

        Vec3 worldStart = originPose == null ? start : originPose.transformPosition(start);
        Vec3 worldEnd = originPose == null ? end : originPose.transformPosition(end);

        BlockHitResult best = clipInSpace(level, entity, start, end);
        Vec3 bestWorldPos = originPose == null ? best.getLocation() : originPose.transformPosition(best.getLocation());

        if (originPose != null) {
            BlockHitResult worldHit = clipInSpace(level, entity, worldStart, worldEnd);
            if (isCloserHit(worldHit, worldHit.getLocation(), best, bestWorldPos, worldStart)) {
                best = worldHit;
                bestWorldPos = worldHit.getLocation();
            }
        }

        Level clientLevel = Minecraft.getInstance().level;
        for (SubLevelAccess subLevel : sable.getAllIntersecting(clientLevel,
                new BoundingBox3d(worldStart, (Position) worldEnd))) {
            if (subLevel == originSubLevel || !(subLevel instanceof ClientSubLevelAccess clientSubLevel)) continue;
            Pose3dc pose = clientSubLevel.renderPose(partialTicks);
            BlockHitResult subHit = clipInSpace(level, entity,
                    pose.transformPositionInverse(worldStart), pose.transformPositionInverse(worldEnd));
            Vec3 subWorldPos = pose.transformPosition(subHit.getLocation());
            if (isCloserHit(subHit, subWorldPos, best, bestWorldPos, worldStart)) {
                best = subHit;
                bestWorldPos = subWorldPos;
            }
        }

        Vec3 localPos = originPose == null ? bestWorldPos : originPose.transformPositionInverse(bestWorldPos);
        if (best.getType() == HitResult.Type.MISS) {
            return BlockHitResult.miss(localPos, best.getDirection(), best.getBlockPos());
        }
        return new BlockHitResult(localPos, best.getDirection(), best.getBlockPos(), best.isInside());
    }

    private static BlockHitResult clipInSpace(BlockGetter level, Entity entity, Vec3 start, Vec3 end) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, entity));
    }

    private static boolean isCloserHit(BlockHitResult candidate, Vec3 candidateWorldPos,
                                       BlockHitResult best, Vec3 bestWorldPos, Vec3 worldStart) {
        if (candidate.getType() == HitResult.Type.MISS) return false;
        if (best.getType() == HitResult.Type.MISS) return true;
        return candidateWorldPos.distanceToSqr(worldStart) < bestWorldPos.distanceToSqr(worldStart);
    }
}
