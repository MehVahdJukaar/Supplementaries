package net.mehvahdjukaar.supplementaries.common.entities.controllers;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class PathNavigation {
    private static final int MAX_TIME_RECOMPUTE = 20;
    private static final int STUCK_CHECK_INTERVAL = 100;
    private static final float STUCK_THRESHOLD_DISTANCE_FACTOR = 0.25F;

    protected final Mob mob;
    protected final Level level;
    @Nullable
    protected Path path;
    /** desired movement speed used by the path-following controller (blocks/tick). */
    protected double desiredSpeed;
    protected int tickCount;
    protected int lastStuckCheckTick;
    protected Vec3 lastStuckPosition = Vec3.ZERO;
    protected Vec3i timeoutLastNode = Vec3i.ZERO;
    protected long timeoutAccumulated;
    protected long lastTimeoutGameTime;
    protected double timeoutAllowedTicks;
    protected float maxDistanceToWaypoint = 0.5F;
    /**
     * Whether a recomputation has been postponed and should be applied later.
     */
    protected boolean delayedRecomputePending;
    protected long timeLastRecompute;
    protected NodeEvaluator nodeEvaluator;
    @Nullable
    private BlockPos targetPos;
    /**
     * Distance in which a path point counts as target-reaching
     */
    private int reachRange;
    private float maxVisitedNodesMultiplier = 1.0F;
    private final PathFinder pathFinder;
    private boolean isStuck;

    public PathNavigation(Mob mob, Level level) {
        this.mob = mob;
        this.level = level;
        int i = Mth.floor(mob.getAttributeValue(Attributes.FOLLOW_RANGE) * 16.0);
        this.pathFinder = this.createPathFinder(i);
    }

    public void resetMaxVisitedNodesMultiplier() {
        this.maxVisitedNodesMultiplier = 1.0F;
    }

    public void setMaxVisitedNodesMultiplier(float multiplier) {
        this.maxVisitedNodesMultiplier = multiplier;
    }

    @Nullable
    public BlockPos getTargetPos() {
        return this.targetPos;
    }

    protected abstract PathFinder createPathFinder(int maxVisitedNodes);

    /**
     * Sets the desired movement speed used for following the currently active path.
     *
     * [SPECULATIVE: describes intended usage — used when a caller wants to adjust movement speed during navigation]
     */
    public void setSpeedModifier(double speed) {
        this.desiredSpeed = speed;
    }

    /**
     * Attempt to recompute the path to the stored targetPos if the cooldown has passed.
     * If the recomputation is done immediately the delayed recompute flag is cleared.
     *
     * If called before the cooldown (20 ticks) the recomputation is deferred by setting a flag.
     *
     * [SPECULATIVE: This documents intended behaviour around delayed recompute semantics.]
     */
    public void recomputePath() {
        if (this.level.getGameTime() - this.timeLastRecompute > MAX_TIME_RECOMPUTE) {
            if (this.targetPos != null) {
                this.path = null;
                this.path = this.createPath(this.targetPos, this.reachRange);
                this.timeLastRecompute = this.level.getGameTime();
                this.delayedRecomputePending = false;
            }
        } else {
            this.delayedRecomputePending = true;
        }
    }

    @Nullable
    public final Path createPath(double x, double y, double z, int accuracy) {
        return this.createPath(BlockPos.containing(x, y, z), accuracy);
    }

    @Nullable
    public Path createPath(Stream<BlockPos> targets, int accuracy) {
        return this.createPath((Set<BlockPos>)targets.collect(Collectors.toSet()), 8, false, accuracy);
    }

    @Nullable
    public Path createPath(Set<BlockPos> positions, int distance) {
        return this.createPath(positions, 8, false, distance);
    }

    @Nullable
    public Path createPath(BlockPos pos, int accuracy) {
        return this.createPath(ImmutableSet.of(pos), 8, false, accuracy);
    }

    @Nullable
    public Path createPath(BlockPos pos, int regionOffset, int accuracy) {
        return this.createPath(ImmutableSet.of(pos), 8, false, regionOffset, accuracy);
    }

    @Nullable
    public Path createPath(Entity entity, int accuracy) {
        return this.createPath(ImmutableSet.of(entity.blockPosition()), 16, true, accuracy);
    }

    @Nullable
    protected Path createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy) {
        return this.createPath(targets, regionOffset, offsetUpward, accuracy, (float)this.mob.getAttributeValue(Attributes.FOLLOW_RANGE));
    }

    /**
     * Creates a path to any of the specified target positions using the PathFinder.
     *
     * Constructs a PathNavigationRegion centered on the mob and asks pathFinder to find a route.
     * If a path is returned the navigation stores the final target and reachRange and resets
     * the stuck-timeout trackers.
     */
    @Nullable
    protected Path createPath(Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy, float followRange) {
        if (targets.isEmpty()) {
            return null;
        } else if (this.mob.getY() < this.level.getMinBuildHeight()) {
            return null;
        } else if (!this.canUpdatePath()) {
            return null;
        } else if (this.path != null && !this.path.isDone() && targets.contains(this.targetPos)) {
            return this.path;
        } else {
            this.level.getProfiler().push("pathfind");
            BlockPos searchCenter = offsetUpward ? this.mob.blockPosition().above() : this.mob.blockPosition();
            int radius = (int)(followRange + regionOffset);
            PathNavigationRegion pathNavigationRegion = new PathNavigationRegion(this.level, searchCenter.offset(-radius, -radius, -radius), searchCenter.offset(radius, radius, radius));
            Path foundPath = this.pathFinder.findPath(pathNavigationRegion, this.mob, targets, followRange, accuracy, this.maxVisitedNodesMultiplier);
            this.level.getProfiler().pop();
            if (foundPath != null && foundPath.getTarget() != null) {
                this.targetPos = foundPath.getTarget();
                this.reachRange = accuracy;
                this.resetStuckTimeout();
            }

            return foundPath;
        }
    }

    public boolean moveTo(double x, double y, double z, double speed) {
        return this.moveTo(this.createPath(x, y, z, 1), speed);
    }

    public boolean moveTo(double x, double y, double z, int accuracy, double speed) {
        return this.moveTo(this.createPath(x, y, z, accuracy), speed);
    }

    public boolean moveTo(Entity entity, double speed) {
        Path path = this.createPath(entity, 1);
        return path != null && this.moveTo(path, speed);
    }

    public boolean moveTo(@Nullable Path newPath, double speed) {
        if (newPath == null) {
            this.path = null;
            return false;
        } else {
            if (!newPath.sameAs(this.path)) {
                this.path = newPath;
            }

            if (this.isDone()) {
                return false;
            } else {
                this.trimPath();
                if (this.path.getNodeCount() <= 0) {
                    return false;
                } else {
                    this.desiredSpeed = speed;
                    Vec3 currentPosition = this.getTempMobPos();
                    this.lastStuckCheckTick = this.tickCount;
                    this.lastStuckPosition = currentPosition;
                    return true;
                }
            }
        }
    }

    @Nullable
    public Path getPath() {
        return this.path;
    }

    public void tick() {
        this.tickCount++;
        if (this.delayedRecomputePending) {
            this.recomputePath();
        }

        if (!this.isDone()) {
            if (this.canUpdatePath()) {
                this.followThePath();
            } else if (this.path != null && !this.path.isDone()) {
                //if we cant update we can still update the path when its directly below us and we are falling
                Vec3 currentPosition = this.getTempMobPos();
                Vec3 nextEntityPos = this.path.getNextEntityPos(this.mob);
                if (currentPosition.y > nextEntityPos.y && !this.mob.onGround()
                        && Mth.floor(currentPosition.x) == Mth.floor(nextEntityPos.x)
                        && Mth.floor(currentPosition.z) == Mth.floor(nextEntityPos.z)) {
                    this.path.advance();
                }
            }

            DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
            if (!this.isDone()) {
                Vec3 nextEntityPos = this.path.getNextEntityPos(this.mob);
                this.mob.getMoveControl().setWantedPosition(nextEntityPos.x, this.getGroundY(nextEntityPos), nextEntityPos.z, this.desiredSpeed);
            }
        }
    }

    /**
     * Given the candidate 3D position, returns a Y value grounded to the floor if necessary
     * so that move control will move the mob to a sensible ground Y coordinate.
     */
    protected double getGroundY(Vec3 vec) {
        BlockPos blockPos = BlockPos.containing(vec);
        return this.level.getBlockState(blockPos.below()).isAir() ? vec.y : WalkNodeEvaluator.getFloorLevel(this.level, blockPos);
    }

    /**
     * Steps along the current path, advances nodes when close enough or when corner-cutting,
     * and runs stuck detection.
     *
     * [SPECULATIVE]
     */
    protected void followThePath() {
        Vec3 currentPosition = this.getTempMobPos();
        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
        Vec3i nextNodePos = this.path.getNextNodePos();
        double dx = Math.abs(this.mob.getX() - (nextNodePos.getX() + 0.5));
        double dy = Math.abs(this.mob.getY() - nextNodePos.getY());
        double dz = Math.abs(this.mob.getZ() - (nextNodePos.getZ() + 0.5));
        boolean closeEnough = dx < this.maxDistanceToWaypoint && dz < this.maxDistanceToWaypoint && dy < 1.0;
        if (closeEnough || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(currentPosition)) {
            this.path.advance();
        }

        this.doStuckDetection(currentPosition);
    }

    //should I target next node. Should advance basically

    /**
     * If you can go directly to the next node — advance.
     *
     * Otherwise, if the path after the next node brings you back toward the mob (or you're already extremely close to next),
     * advance only if the next-after node is either closer or the directions to next and second-next are opposed
     * (path doubles back). Otherwise, do not advance.
     */
    private boolean shouldTargetNextNodeInDirection(Vec3 currentPosition) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            //is at end of path
            return false;
        } else {
            Vec3 nextNodePos = Vec3.atBottomCenterOf(this.path.getNextNodePos());
            if (!currentPosition.closerThan(nextNodePos, 2.0)) {
                //I am too close to the next node. Guess I'm not moving at all?
                //weird considering there's already distance check before this
                return false;
            } else if (this.canMoveDirectly(currentPosition, this.path.getNextEntityPos(this.mob))) {
                //if path is clear means I can walk to next node correctly. target it.
                return true;
            } else {
                //i cant move directly towards my next goal. whats going on? IDK TBH
                Vec3 secondNextNodePos = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
                Vec3 toNext = nextNodePos.subtract(currentPosition);
                Vec3 toSecondNext = secondNextNodePos.subtract(currentPosition);
                double distNextSqr = toNext.lengthSqr();
                double distSecondNextSqr = toSecondNext.lengthSqr();
                boolean secondNextIsCloser = distSecondNextSqr < distNextSqr;
                boolean currentIsVeryClose = distNextSqr < 0.5;
                if (!secondNextIsCloser && !currentIsVeryClose) {
                    //if second next is further away from my pos than the previous and I'm not very close
                    return false;
                } else {
                    Vec3 dirToNext = toNext.normalize();
                    Vec3 dirToSecondNext = toSecondNext.normalize();
                    //if they point opposite directions
                    return dirToSecondNext.dot(dirToNext) < 0.0;
                }
            }
        }
    }

    /**
     * Checks whether the entity appears to be stuck by comparing distance moved since the last check
     * and also enforces a per-node timeout that can cancel navigation if the mob spends too long near a node.
     *
     * @param currentPosition the current position of the mob (usually obtained from getTempMobPos())
     */
    protected void doStuckDetection(Vec3 currentPosition) {
        if (this.tickCount - this.lastStuckCheckTick > STUCK_CHECK_INTERVAL) {
            float speedValue = this.mob.getSpeed() >= 1.0F ? this.mob.getSpeed() : this.mob.getSpeed() * this.mob.getSpeed();
            float threshold = speedValue * STUCK_CHECK_INTERVAL * STUCK_THRESHOLD_DISTANCE_FACTOR;
            // square comparisons use distanceToSqr, so we square threshold
            if (currentPosition.distanceToSqr(this.lastStuckPosition) < threshold * threshold) {
                this.isStuck = true;
                this.stop();
            } else {
                this.isStuck = false;
            }

            this.lastStuckCheckTick = this.tickCount;
            this.lastStuckPosition = currentPosition;
        }

        if (this.path != null && !this.path.isDone()) {
            Vec3i currentPathNode = this.path.getNextNodePos();
            long gameTime = this.level.getGameTime();
            if (currentPathNode.equals(this.timeoutLastNode)) {
                this.timeoutAccumulated = this.timeoutAccumulated + (gameTime - this.lastTimeoutGameTime);
            } else {
                this.timeoutLastNode = currentPathNode;
                double distanceToNode = currentPosition.distanceTo(Vec3.atBottomCenterOf(this.timeoutLastNode));
                this.timeoutAllowedTicks = this.mob.getSpeed() > 0.0F ? distanceToNode / this.mob.getSpeed() * 20.0 : 0.0;
            }

            if (this.timeoutAllowedTicks > 0.0 && this.timeoutAccumulated > this.timeoutAllowedTicks * 3.0) {
                this.timeoutPath();
            }

            this.lastTimeoutGameTime = gameTime;
        }
    }

    private void timeoutPath() {
        this.resetStuckTimeout();
        this.stop();
    }

    private void resetStuckTimeout() {
        this.timeoutLastNode = Vec3i.ZERO;
        this.timeoutAccumulated = 0L;
        this.timeoutAllowedTicks = 0.0;
        this.isStuck = false;
    }

    public boolean isDone() {
        return this.path == null || this.path.isDone();
    }

    public boolean isInProgress() {
        return !this.isDone();
    }

    public void stop() {
        this.path = null;
    }

    protected abstract Vec3 getTempMobPos();

    /**
     * Returns true if the navigator is currently allowed to update/follow a path
     * (typically true when on ground or when swimming and swim-capable).
     */
    protected abstract boolean canUpdatePath();

    /**
     * Trims path nodes that sit on cauldrons by raising those nodes by one block height.
     *
     * This prevents mobs from trying to path through a cauldron location at the wrong vertical offset.
     */
    protected void trimPath() {
        if (this.path != null) {
            for (int i = 0; i < this.path.getNodeCount(); i++) {
                Node node = this.path.getNode(i);
                Node node2 = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
                BlockState blockState = this.level.getBlockState(new BlockPos(node.x, node.y, node.z));
                if (blockState.is(BlockTags.CAULDRONS)) {
                    this.path.replaceNode(i, node.cloneAndMove(node.x, node.y + 1, node.z));
                    if (node2 != null && node.y >= node2.y) {
                        this.path.replaceNode(i + 1, node.cloneAndMove(node2.x, node.y + 1, node2.z));
                    }
                }
            }
        }
    }

    protected boolean canMoveDirectly(Vec3 posVec31, Vec3 posVec32) {
        return false;
    }

    //if can go diagonally
    public boolean canCutCorner(PathType pathType) {
        return pathType != PathType.DANGER_FIRE && pathType != PathType.DANGER_OTHER && pathType != PathType.WALKABLE_DOOR;
    }

    // raycast toward direction to see if direct path is clear
    protected static boolean isClearForMovementBetween(Mob mob, Vec3 pos1, Vec3 pos2, boolean allowSwimming) {
        Vec3 targetMid = new Vec3(pos2.x, pos2.y + mob.getBbHeight() * 0.5, pos2.z);
        return mob.level()
                .clip(new ClipContext(pos1, targetMid, ClipContext.Block.COLLIDER, allowSwimming ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, mob))
                .getType()
                == HitResult.Type.MISS;
    }

    /**
     * Returns true if the given BlockPos is a stable location (solid block below).
     * Mostly used for some niche goals
     */
    public boolean isStableDestination(BlockPos pos) {
        BlockPos blockPos = pos.below();
        return this.level.getBlockState(blockPos).isSolidRender(this.level, blockPos);
    }

    public NodeEvaluator getNodeEvaluator() {
        return this.nodeEvaluator;
    }

    public void setCanFloat(boolean canSwim) {
        this.nodeEvaluator.setCanFloat(canSwim);
    }

    public boolean canFloat() {
        return this.nodeEvaluator.canFloat();
    }

    /**
     * Returns whether a recompute should be performed for the provided position.
     *
     * The default implementation prevents recomputation when a recompute is already delayed,
     * and otherwise checks if the proposed position is sufficiently close to the midpoint between
     * mob and path end to justify recomputing.
     */
    public boolean shouldRecomputePath(BlockPos pos) {
        if (this.delayedRecomputePending) {
            return false;
        } else if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
            // if the position is closer to the path end than the mob is, we should recompute
            Node node = this.path.getEndNode();
            Vec3 midpoint = new Vec3((node.x + this.mob.getX()) / 2.0, (node.y + this.mob.getY()) / 2.0, (node.z + this.mob.getZ()) / 2.0);
            return pos.closerToCenterThan(midpoint, this.path.getNodeCount() - this.path.getNextNodeIndex());
        } else {
            return false;
        }
    }

    public float getMaxDistanceToWaypoint() {
        return this.maxDistanceToWaypoint;
    }

    public boolean isStuck() {
        return this.isStuck;
    }
}
