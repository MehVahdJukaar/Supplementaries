package net.mehvahdjukaar.supplementaries.common.entities.controllers;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

// Surface only pathing for a mob driving a boat. Nodes are the top water block of a column.
// A boat can never climb, so neighbors are same level or a drop. Anything that is not water is impassable.
public class BoatNodeEvaluator extends NodeEvaluator {

    // a floating hull bottom sits roughly this far above the water block bottom
    private static final double HULL_DRAFT = 0.4;
    private static final int MAX_DROP = 3;
    private static final float WATER_MALUS = 0;
    private static final float IMPASSABLE = -1;

    private final Long2ObjectMap<PathType> pathTypeCache = new Long2ObjectOpenHashMap<>();
    private final Node[] straightNeighbors = new Node[Direction.Plane.HORIZONTAL.length()];
    private Entity boat;

    @Override
    public void prepare(PathNavigationRegion level, Mob mob) {
        super.prepare(level, mob);
        Entity vehicle = mob.getVehicle();
        this.boat = vehicle != null ? vehicle : mob;
        this.entityWidth = Mth.floor(boat.getBbWidth() + 1.0F);
        this.entityDepth = this.entityWidth;
        // headroom is for the rider. The hull itself is short
        this.entityHeight = Mth.floor(mob.getBbHeight() + 1.0F);
    }

    @Override
    public void done() {
        this.pathTypeCache.clear();
        this.boat = null;
        super.done();
    }

    @Override
    public Node getStart() {
        int x = footprintOrigin(boat.getX());
        int z = footprintOrigin(boat.getZ());
        int y = findSurfaceY(boat.getBlockX(), boat.getBlockY(), boat.getBlockZ());
        Node node = getNode(x, y, z);
        node.type = getCachedPathType(x, y, z);
        node.costMalus = getMalus(node.type);
        return node;
    }

    // node whose footprint center is closest to the given coordinate. Same convention as Path.getEntityPosAtNode
    private int footprintOrigin(double coord) {
        return Mth.floor(coord - entityWidth * 0.5 + 0.5);
    }

    // top water block of the column, or the block itself when the boat is beached
    private int findSurfaceY(int x, int y, int z) {
        CollisionGetter level = currentContext.level();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        if (!level.getFluidState(pos).is(FluidTags.WATER)) {
            // hull resting on the bottom in shallow water
            if (level.getFluidState(pos.move(Direction.DOWN)).is(FluidTags.WATER)) {
                y--;
            } else {
                return y;
            }
        }
        while (level.getFluidState(pos.set(x, y + 1, z)).is(FluidTags.WATER)) {
            y++;
        }
        return y;
    }

    @Override
    public Target getTarget(double x, double y, double z) {
        return getTargetNodeAt(x, y, z);
    }

    @Override
    public int getNeighbors(Node[] outputArray, Node node) {
        int count = 0;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            Node neighbor = findNeighbor(node.x + dir.getStepX(), node.y, node.z + dir.getStepZ());
            straightNeighbors[dir.get2DDataValue()] = neighbor;
            if (isNeighborValid(neighbor)) {
                outputArray[count++] = neighbor;
            }
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            Direction clockWise = dir.getClockWise();
            Node side1 = straightNeighbors[dir.get2DDataValue()];
            Node side2 = straightNeighbors[clockWise.get2DDataValue()];
            if (canCutDiagonal(node, side1) && canCutDiagonal(node, side2)) {
                Node neighbor = findNeighbor(node.x + dir.getStepX() + clockWise.getStepX(), node.y,
                        node.z + dir.getStepZ() + clockWise.getStepZ());
                if (isNeighborValid(neighbor)) {
                    outputArray[count++] = neighbor;
                }
            }
        }
        return count;
    }

    private static boolean isNeighborValid(@Nullable Node neighbor) {
        return neighbor != null && !neighbor.closed && neighbor.costMalus >= 0;
    }

    // the hull would clip the corner block otherwise
    private static boolean canCutDiagonal(Node from, @Nullable Node side) {
        return side != null && side.costMalus >= 0 && side.y == from.y;
    }

    @Nullable
    private Node findNeighbor(int x, int y, int z) {
        PathType type = getCachedPathType(x, y, z);
        if (type == PathType.WATER) {
            return getNodeWithType(x, y, z, type);
        }
        if (type == PathType.OPEN) {
            for (int drop = 1; drop <= MAX_DROP; drop++) {
                PathType below = getCachedPathType(x, y - drop, z);
                if (below == PathType.WATER) {
                    return getNodeWithType(x, y - drop, z, below);
                }
                if (below != PathType.OPEN) break;
            }
        }
        return getNodeWithType(x, y, z, type);
    }

    private Node getNodeWithType(int x, int y, int z, PathType type) {
        Node node = getNode(x, y, z);
        node.type = type;
        node.costMalus = Math.max(node.costMalus, getMalus(type));
        return node;
    }

    private static float getMalus(PathType type) {
        return type == PathType.WATER ? WATER_MALUS : IMPASSABLE;
    }

    private PathType getCachedPathType(int x, int y, int z) {
        return pathTypeCache.computeIfAbsent(BlockPos.asLong(x, y, z),
                l -> getPathTypeOfMob(currentContext, x, y, z, mob));
    }

    @Override
    public PathType getPathType(PathfindingContext context, int x, int y, int z) {
        return getColumnType(context.level(), x, y, z);
    }

    // WATER: hull floats here and the rider has headroom. OPEN: same but dry. BLOCKED: anything else
    @Override
    public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
        boolean anyWater = false;
        for (int dx = 0; dx < entityWidth; dx++) {
            for (int dz = 0; dz < entityDepth; dz++) {
                PathType column = getColumnType(context.level(), x + dx, y, z + dz);
                if (column == PathType.BLOCKED) return PathType.BLOCKED;
                anyWater |= column == PathType.WATER;
            }
        }
        return anyWater ? PathType.WATER : PathType.OPEN;
    }

    private PathType getColumnType(CollisionGetter level, int x, int y, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        FluidState fluid = state.getFluidState();
        boolean water = fluid.is(FluidTags.WATER);
        if (!water && !fluid.isEmpty()) return PathType.BLOCKED;
        if (!hullClears(state, level, pos)) return PathType.BLOCKED;
        for (int dy = 1; dy < entityHeight; dy++) {
            pos.set(x, y + dy, z);
            if (!isHeadroom(level.getBlockState(pos), level, pos)) return PathType.BLOCKED;
        }
        return water ? PathType.WATER : PathType.OPEN;
    }

    // lily pads, carpets and such sit below the floating hull. Slabs and fences dont
    private static boolean hullClears(BlockState state, CollisionGetter level, BlockPos pos) {
        VoxelShape shape = state.getCollisionShape(level, pos);
        return shape.isEmpty() || shape.max(Direction.Axis.Y) <= HULL_DRAFT;
    }

    // water above means the surface is higher there, which the boat cant reach
    private static boolean isHeadroom(BlockState state, CollisionGetter level, BlockPos pos) {
        return state.getFluidState().isEmpty() && state.getCollisionShape(level, pos).isEmpty();
    }
}
