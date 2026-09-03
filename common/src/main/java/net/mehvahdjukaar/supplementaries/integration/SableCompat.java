package net.mehvahdjukaar.supplementaries.integration;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class SableCompat {

    /**
     * Bridges a block entity and an entity that might not share the same space, like a block on a ship and a mob
     * standing outside of it. Their raw coordinates are in different frames so you can't compare them directly
     */
    public static SubLevelTransform subLevelsBetween(BlockEntity blockEntity, Entity entity) {
        SableCompanion sable = SableCompanion.INSTANCE;
        return new SubLevelTransform(sable.getContaining(blockEntity), sable.getContaining(entity));
    }

    public static SubLevelTransform subLevelsBetween(Level level, BlockPos blockPos, Entity entity) {
        SableCompanion sable = SableCompanion.INSTANCE;
        return new SubLevelTransform(sable.getContaining(level, blockPos), sable.getContaining(entity));
    }

    public static final class SubLevelTransform {

        @Nullable
        private final SubLevelAccess blockSubLevel;
        @Nullable
        private final SubLevelAccess entitySubLevel;
        private final boolean sameSpace;

        private SubLevelTransform(@Nullable SubLevelAccess blockSubLevel, @Nullable SubLevelAccess entitySubLevel) {
            this.blockSubLevel = blockSubLevel;
            this.entitySubLevel = entitySubLevel;
            this.sameSpace = blockSubLevel == entitySubLevel ||
                    (blockSubLevel != null && entitySubLevel != null &&
                            blockSubLevel.getUniqueId().equals(entitySubLevel.getUniqueId()));
        }

        public AABB entityBoxToBlockSpace(AABB entityBox) {
            if (sameSpace) return entityBox;
            BoundingBox3d box = new BoundingBox3d(entityBox);
            if (entitySubLevel != null) box.transform(entitySubLevel.logicalPose());
            if (blockSubLevel != null) box.transformInverse(blockSubLevel.logicalPose());
            return new AABB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
        }

        public Vec3 entityPointToBlockSpace(Vec3 point) {
            if (sameSpace) return point;
            Vector3d p = new Vector3d(point.x, point.y, point.z);
            if (entitySubLevel != null) entitySubLevel.logicalPose().transformPosition(p);
            if (blockSubLevel != null) blockSubLevel.logicalPose().transformPositionInverse(p);
            return new Vec3(p.x, p.y, p.z);
        }

        public Vec3 directionToEntitySpace(Vec3 direction) {
            if (sameSpace) return direction;
            Vector3d dir = new Vector3d(direction.x, direction.y, direction.z);
            if (blockSubLevel != null) blockSubLevel.logicalPose().transformNormal(dir);
            if (entitySubLevel != null) entitySubLevel.logicalPose().transformNormalInverse(dir);
            return new Vec3(dir.x, dir.y, dir.z);
        }
    }
}
