package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

public class BoatAwarePathNavigation extends GroundPathNavigation {

    public BoatAwarePathNavigation(Mob strider, Level level) {
        super(strider, level);
    }

    @Nullable
    private Boat getBoat() {
        Entity vehicle = this.mob.getVehicle();
        if (vehicle instanceof Boat boat) {
            return boat;
        }
        return null;
    }

    @Override
    protected boolean hasValidPathType(PathType pathType) {
        if ((pathType == PathType.WATER || pathType == PathType.WATER_BORDER) && getBoat() != null) {
            return true;
        }
        return super.hasValidPathType(pathType);
    }

    @Override
    public boolean isStableDestination(BlockPos pos) {
        Boat boat = getBoat();
        if (boat != null) {
            return super.isStableDestination(pos) || this.level.getBlockState(pos).is(Blocks.WATER);
        }
        return super.isStableDestination(pos);
    }
}
