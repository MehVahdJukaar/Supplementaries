package net.mehvahdjukaar.supplementaries.common.entities.controllers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BoatPathNavigation extends PathNavigation {

    public BoatPathNavigation(Mob strider, Level level) {
        super(strider, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        return null;
    }

    @Override
    protected Vec3 getTempMobPos() {
        return null;
    }

    @Override
    protected boolean canUpdatePath() {
        return false;
    }


}
