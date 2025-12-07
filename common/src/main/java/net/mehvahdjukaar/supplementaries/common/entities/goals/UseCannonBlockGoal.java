package net.mehvahdjukaar.supplementaries.common.entities.goals;

import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.entities.PlundererEntity;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.stream.Stream;

import static net.mehvahdjukaar.supplementaries.common.entities.goals.UseCannonAICommon.*;

public class UseCannonBlockGoal extends MoveToBlockGoal {

    private final int searchRange;

    private int igniteCannonCooldown = 0;
    private int atCannonTicks = 0;
    private int ticksSinceShot = 0;

    public UseCannonBlockGoal(PathfinderMob mob, double speedModifier, int searchRange) {
        super(mob, speedModifier, searchRange);
        this.searchRange = searchRange;
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
    public void stop() {
        super.stop();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isPassenger() || !hasTargetInCannonRange(mob)) return false;
        if (isReachedTarget() && ticksSinceShot > MAX_TIME_WITHOUT_SHOOTING) {
            // return false;
        }
        return this.atCannonTicks <= MAX_STAY_TIME && this.tryTicks <= MAX_GO_TO_TIME && this.isValidTarget(this.mob.level(), this.blockPos);
    }

    @Override
    public boolean canUse() {
        if (this.mob.isPassenger() || !hasTargetInCannonRange(mob)) return false;
        return super.canUse();
    }

    @Override
    protected int nextStartTick(PathfinderMob creature) {
        return reducedTickDelay(GOAL_INTERVAL);
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

            Level level = mob.level();
            var cannonTile = (CannonBlockTile) level.getBlockEntity(this.blockPos);

            atCannonTicks++;
            ticksSinceShot++;
            //shoot
            if (igniteCannonCooldown > 0) {
                igniteCannonCooldown--;
            }
            boolean canShoot = igniteCannonCooldown <= 0;
            //check if we are in the way and move out incase we are
            Vec3 center = Vec3.atCenterOf(cannonTile.getBlockPos());
            Vec3 targetPos = mob.getTarget().position();
            Vec3 myPos = mob.position();

            Vec3 toTarget = targetPos.subtract(center);
            Vec3 toMe = myPos.subtract(center);

            double dot = toTarget.normalize().dot(toMe.normalize());

            double t = toMe.dot(toTarget.normalize());
            boolean between = t > 0 && t < toTarget.length();
            if (!mob.getNavigation().isDone()) {
                //    return;
            }

            if (dot > 0.6 && between) { // only block if aligned AND between
                Direction wantedDir = Direction.getNearest(-toTarget.x, -toTarget.y, -toTarget.z);
                if (ticksSinceShot % 4 == 0) moveAroundCannon(wantedDir);
                return;
            }
            this.mob.getLookControl().setLookAt(mob.getTarget());

            if (aimCannonAndShoot(cannonTile.selfAccess, mob, mob.getTarget(), canShoot)) {
                igniteCannonCooldown = Mth.randomBetweenInclusive(level.random, SHOOTING_COOLDOWN / 2, SHOOTING_COOLDOWN); //random delay between shots
                ticksSinceShot = 0;
            }
        }
    }

    protected void moveAroundCannon(Direction wantedDir) {
        BlockPos cannonPos = this.getMoveToTarget();
        BlockPos targetPos = cannonPos.relative(wantedDir);
        this.mob.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, this.speedModifier);
    }


}
