package net.mehvahdjukaar.supplementaries.common.entities.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class BoatPathNavigation extends PathNavigation {

    public BoatPathNavigation(Mob strider, Level level) {
        super(strider, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new BoatNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        this.nodeEvaluator.setCanFloat(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }

    @Override
    protected boolean canUpdatePath() {
        return isBoatInWater();
    }

    private boolean isBoatInWater() {
        Entity boat = this.mob.getControlledVehicle();
        //we assume we are always in a boat. here we just check if boat is on water
        return boat != null && boat.isInWater() && !boat.isUnderWater();
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.getSurfaceY(), this.mob.getZ());
    }

    @Override
    protected double getGroundY(Vec3 vec) {
        BlockPos posAt = BlockPos.containing(vec);
        return BoatNodeEvaluator.getWaterHeightLevel(level, posAt);
    }


    /**
     * Gets the safe pathing Y position for the entity depending on if it can path swim or not
     */
    private int getSurfaceY() {
        int i = 0;
        int j = 0;
        FluidState blockState;
        do {
            blockState = this.level.getFluidState(BlockPos.containing(this.mob.getX(), ++i, this.mob.getZ()));
            if (++j > 16) {
                return this.mob.getBlockY();
            }
        }
        while (blockState.is(FluidTags.WATER));
        return i;
    }

    @Override
    public void setCanFloat(boolean canSwim) {
    }

    @Override
    public boolean canFloat() {
        return true;
    }

    //idk tbh. same as ground path nav
    @Override
    public Path createPath(Entity entity, int accuracy) {
        return this.createPath(entity.blockPosition(), accuracy);
    }

    //adjust end point to always be above solid
    @Override
    public Path createPath(BlockPos pos, int accuracy) {
        LevelChunk levelChunk = this.level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if (levelChunk == null) {
            return null;
        } else {
            //in air. go below
            if (levelChunk.getBlockState(pos).isAir()) {
                BlockPos blockPos = pos.below();

                while (blockPos.getY() > this.level.getMinBuildHeight() && levelChunk.getBlockState(blockPos).isAir()) {
                    blockPos = blockPos.below();
                }

                if (blockPos.getY() > this.level.getMinBuildHeight()) {
                    return super.createPath(blockPos.above(), accuracy);
                }

                while (blockPos.getY() < this.level.getMaxBuildHeight() && levelChunk.getBlockState(blockPos).isAir()) {
                    blockPos = blockPos.above();
                }

                pos = blockPos;
            }

            //in ground. go above
            if (!levelChunk.getFluidState(pos).is(FluidTags.WATER)) {
                return super.createPath(pos, accuracy);
            } else {
                BlockPos blockPos = pos.above();

                while (blockPos.getY() < this.level.getMaxBuildHeight() && levelChunk.getFluidState(blockPos)
                        .is(FluidTags.WATER)) {
                    blockPos = blockPos.above();
                }

                return super.createPath(blockPos, accuracy);
            }
        }
    }


    //where can i stay without falling i guess?
    @Override
    public boolean isStableDestination(BlockPos pos) {
        return super.isStableDestination(pos) ||
                level.getFluidState(pos).is(FluidTags.WATER);
    }
}
