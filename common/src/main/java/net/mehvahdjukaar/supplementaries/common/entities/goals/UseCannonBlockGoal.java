package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.stream.Stream;

import static net.mehvahdjukaar.supplementaries.common.entities.goals.UseCannonBoatBehavior.aimCannonAndShoot;

public class UseCannonBlockGoal extends MoveToBlockGoal {

    private static final int MAX_STAY_TIME = 1000;
    private static final int MAX_GO_TO_TIME = 1200;
    private static final int MIN_CANNON_RANGE = 16;
    private static final int GOAL_INTERVAL = 200;
    private static final int SHOOTING_COOLDOWN = 40;

    private final int searchRange;
    private final PlundererEntity plunderer;

    private int attackDelay = 0;
    private int ticksUsingCannon = 0;

    public UseCannonBlockGoal(PlundererEntity mob, double speedModifier, int searchRange) {
        super(mob, speedModifier, searchRange);
        this.searchRange = searchRange;
        this.plunderer = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public double acceptedDistance() {
        return 1.75f;
    }

    @Override
    protected BlockPos getMoveToTarget() {
        return blockPos;
    }

    @Override
    public void start() {
        super.start();
        //max action ticks basically
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isPassenger() || !hasTargetInCannonRange()) return false;
        return this.tryTicks >= -MAX_STAY_TIME && this.tryTicks <= MAX_GO_TO_TIME && this.isValidTarget(this.mob.level(), this.blockPos);
    }

    @Override
    public boolean canUse() {
        if (this.mob.isPassenger() || !hasTargetInCannonRange()) return false;
        return super.canUse();
    }

    @Override
    protected int nextStartTick(PathfinderMob creature) {
        return GOAL_INTERVAL;
    }

    private boolean hasTargetInCannonRange() {
        var target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        double distSq = mob.distanceToSqr(target);
        return distSq > MIN_CANNON_RANGE * MIN_CANNON_RANGE;
    }

    @Override
    protected boolean findNearestBlock() {
        PoiManager poiManager = ((ServerLevel) mob.level()).getPoiManager();
        Stream<PoiRecord> stream = poiManager.getInRange((holder) ->
                        holder.value() == ModEntities.USABLE_CANNON.get(),
                this.mob.blockPosition(), this.searchRange, PoiManager.Occupancy.ANY);

        for (var p : stream.toList()) {
            BlockPos pos = p.getPos();
            if (this.mob.isWithinRestriction(pos) && this.isValidTarget(this.mob.level(), pos)) {
                this.blockPos = pos;
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        return (be instanceof CannonBlockTile cb && cb.canBeUsedBy(pos, this.mob) && cb.hasFuelAndProjectiles());
    }

    //idk why its not like this before
    @Override
    protected void moveMobToBlock() {
        BlockPos actualTarget = this.getMoveToTarget();
        this.mob.getNavigation().moveTo(actualTarget.getX() + 0.5, actualTarget.getY(), actualTarget.getZ() + 0.5, this.speedModifier);
    }

    @Override
    public void tick() {
        super.tick();
        if (isReachedTarget()) {

            this.mob.getLookControl().setLookAt(mob.getTarget());
            Level level = mob.level();
            var cannonTile = (CannonBlockTile) level.getBlockEntity(this.blockPos);

            ticksUsingCannon++;
            //shoot
            if (attackDelay > 0) {
                attackDelay--;
            }
            boolean canShoot = attackDelay <= 0;
            //check if we are in the way and move out incase we are
            Vec3 center = Vec3.atCenterOf(cannonTile.getBlockPos());
            Vec3 targetPos = mob.getTarget().position();
            Vec3 myPos = mob.position();

            Vec3 toTarget = targetPos.subtract(center);
            Vec3 toMe = myPos.subtract(center);

            double dot = toTarget.normalize().dot(toMe.normalize());

            double t = toMe.dot(toTarget.normalize());
            boolean between = t > 0 && t < toTarget.length();

            if (dot > 0.6 && between) { // only block if aligned AND between
                Direction wantedDir = Direction.getNearest(-toTarget.x, -toTarget.y, -toTarget.z);
                moveAroundCannon(wantedDir);
                return;
            }

            if (!mob.getNavigation().isDone()) return;

            if (aimCannonAndShoot(cannonTile.selfAccess, mob, mob.getTarget(), canShoot)) {
                attackDelay = Mth.randomBetweenInclusive(level.random, 20, 40); //random delay between shots
            }
        }
    }

    protected void moveAroundCannon(Direction wantedDir) {
        BlockPos cannonPos = this.getMoveToTarget();
        BlockPos targetPos = cannonPos.relative(wantedDir);
        this.mob.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, this.speedModifier);
    }


}
