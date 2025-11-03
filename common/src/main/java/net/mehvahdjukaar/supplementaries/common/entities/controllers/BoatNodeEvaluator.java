package net.mehvahdjukaar.supplementaries.common.entities.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.*;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BoatNodeEvaluator extends NodeEvaluator {


    //like WalkNodeEvaluator.getFloorLevel
    public static double getWaterHeightLevel(BlockGetter level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState stateBelow = level.getBlockState(belowPos);
        FluidState fluidState = stateBelow.getFluidState();
        VoxelShape voxelShape = stateBelow.getCollisionShape(level, belowPos);
        double solidShape = belowPos.getY() + (voxelShape.isEmpty() ? 0.0 : voxelShape.max(Direction.Axis.Y));
        float fluidHeight = fluidState.getHeight(level, belowPos);
        return Math.max(solidShape, fluidHeight);
    }

    @Override
    public Node getStart() {
        return null;
    }

    @Override
    public Target getTarget(double x, double y, double z) {
        return null;
    }

    @Override
    public int getNeighbors(Node[] outputArray, Node node) {
        return 0;
    }

    @Override
    public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) {
        return null;
    }

    @Override
    public PathType getPathType(PathfindingContext context, int x, int y, int z) {
        return null;
    }



}
