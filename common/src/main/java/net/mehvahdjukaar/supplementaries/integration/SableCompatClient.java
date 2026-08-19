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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SableCompatClient {

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
