package net.mehvahdjukaar.supplementaries.common.entities.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

// All positions refer to the boat, not the rider sitting in it
public class BoatPathNavigation extends PathNavigation {

    // boats cover ground fast and mobs have short follow ranges
    private static final float MIN_FOLLOW_RANGE = 24;
    private static final int VISITED_NODES_MULTIPLIER = 2;

    public BoatPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new BoatNodeEvaluator();
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes * VISITED_NODES_MULTIPLIER);
    }

    private Entity getBoat() {
        Entity vehicle = this.mob.getControlledVehicle();
        return vehicle != null ? vehicle : this.mob;
    }

    @Override
    protected boolean canUpdatePath() {
        Entity vehicle = this.mob.getControlledVehicle();
        // sinking boats eject their passengers anyway. Beached ones are still allowed to path back to water
        return vehicle != null && !vehicle.isUnderWater();
    }

    @Override
    protected Vec3 getTempMobPos() {
        return getBoat().position();
    }

    @Override
    protected double getGroundY(Vec3 vec) {
        return getBoat().getY();
    }

    // same as super but waypoints are computed for the hull width, not the rider width
    @Override
    public void tick() {
        this.tick++;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }
        if (this.isDone()) return;

        if (this.canUpdatePath()) {
            this.followThePath();
        }
        DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
        if (!this.isDone()) {
            Vec3 wanted = getNextWaypoint();
            this.mob.getMoveControl().setWantedPosition(wanted.x, this.getGroundY(wanted), wanted.z, this.speedModifier);
        }
    }

    private Vec3 getNextWaypoint() {
        return this.path.getEntityPosAtNode(getBoat(), this.path.getNextNodeIndex());
    }

    @Override
    protected void followThePath() {
        Entity boat = getBoat();
        Vec3 pos = this.getTempMobPos();
        this.maxDistanceToWaypoint = boat.getBbWidth() > 0.75F ? boat.getBbWidth() / 2.0F : 0.75F - boat.getBbWidth() / 2.0F;
        Vec3 waypoint = getNextWaypoint();
        double dx = Math.abs(pos.x - waypoint.x);
        double dy = Math.abs(pos.y - waypoint.y);
        double dz = Math.abs(pos.z - waypoint.z);
        boolean reached = dx < this.maxDistanceToWaypoint && dz < this.maxDistanceToWaypoint && dy < 1.0;
        if (reached || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(pos)) {
            this.path.advance();
        }
        this.doStuckDetection(pos);
    }

    @Override
    protected boolean canMoveDirectly(Vec3 from, Vec3 to) {
        return isClearForMovementBetween(this.mob, from, to, false);
    }

    @Override
    protected Path createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy) {
        float followRange = (float) this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        return this.createPath(targets, regionOffset, offsetUpward, accuracy, Math.max(followRange, MIN_FOLLOW_RANGE));
    }

    @Override
    public Path createPath(Entity entity, int accuracy) {
        return this.createPath(entity.blockPosition(), accuracy);
    }

    @Override
    public Path createPath(BlockPos pos, int accuracy) {
        LevelChunk chunk = this.level.getChunkSource().getChunkNow(
                SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if (chunk == null) return null;
        return super.createPath(toSurfaceTarget(chunk, pos), accuracy);
    }

    // nodes sit on the top water block, so aim there when the target is in or above water.
    private BlockPos toSurfaceTarget(LevelChunk chunk, BlockPos pos) {
        BlockPos.MutableBlockPos p = pos.mutable();
        while (p.getY() > this.level.getMinBuildHeight() && chunk.getBlockState(p).isAir()) {
            p.move(Direction.DOWN);
        }
        if (!chunk.getFluidState(p).is(FluidTags.WATER)) return pos;
        while (p.getY() < this.level.getMaxBuildHeight() && chunk.getFluidState(p.above()).is(FluidTags.WATER)) {
            p.move(Direction.UP);
        }
        return p.immutable();
    }

    @Override
    public boolean isStableDestination(BlockPos pos) {
        return super.isStableDestination(pos) || this.level.getFluidState(pos).is(FluidTags.WATER);
    }

    @Override
    public void setCanFloat(boolean canSwim) {
    }

    @Override
    public boolean canFloat() {
        return true;
    }
}
