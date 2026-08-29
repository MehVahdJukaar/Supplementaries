package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.common.entities.goals.AbandonShipGoal;
import net.mehvahdjukaar.supplementaries.common.entities.goals.BoardBoatGoal;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.material.FluidState;

import java.util.ArrayList;
import java.util.List;

public final class NavalRaidSpawner {

    private static final int BOAT_SPREAD = 4;
    private static final int BOAT_POS_ATTEMPTS = 8;
    private static final float PLUNDERER_REPLACE_CHANCE = 0.5F;
    private static final int BOARD_BOAT_GOAL_PRIORITY = 2;
    private static final int ABANDON_SHIP_GOAL_PRIORITY = 3;
    private static final int BOARD_BOAT_TRY_INTERVAL = 40;

    public static boolean isEnabled() {
        return CommonConfigs.Functional.PLUNDERER_ENABLED.get() && CommonConfigs.Functional.NAVAL_RAID_CHANCE.get() > 0;
    }

    public static boolean acceptsWaterSpawnPos(ServerLevel level, BlockPos heightmapPos) {
        return isEnabled()
                && level.random.nextFloat() < CommonConfigs.Functional.NAVAL_RAID_CHANCE.get()
                && isOpenWater(level, heightmapPos);
    }

    public static boolean isNavalWave(ServerLevel level, BlockPos waveSpawnPos) {
        return isEnabled() && level.getFluidState(waveSpawnPos.below()).is(FluidTags.WATER);
    }

    // only some pillagers become captains. The rest swim and hitch a ride
    public static EntityType<?> navalRaiderType(EntityType<?> type, RandomSource random) {
        boolean becomesPlunderer = type == EntityType.PILLAGER && random.nextFloat() < PLUNDERER_REPLACE_CHANCE;
        return becomesPlunderer ? ModEntities.PLUNDERER.get() : type;
    }

    private static boolean isOpenWater(ServerLevel level, BlockPos heightmapPos) {
        if (!level.getBlockState(heightmapPos).isAir()) return false;
        BlockPos surface = heightmapPos.below();
        if (!isStillWater(level, surface.below())) return false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!isStillWater(level, surface.offset(dx, 0, dz))) return false;
            }
        }
        return true;
    }

    private static boolean isStillWater(ServerLevel level, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos);
        return fluid.is(FluidTags.WATER) && fluid.isSource();
    }

    public static void boardBoats(ServerLevel level, Raid raid, int wave, BlockPos spawnPos, List<Raider> spawned) {
        List<PlundererEntity> captains = new ArrayList<>();
        List<Raider> crew = new ArrayList<>();
        for (Raider raider : spawned) {
            boolean isRavagerRider = raider.isPassenger();
            if (isRavagerRider) continue;
            if (raider instanceof PlundererEntity plunderer) {
                captains.add(plunderer);
            } else if (!(raider instanceof Ravager)) {
                crew.add(raider);
            }
        }

        if (captains.isEmpty() && !crew.isEmpty()) {
            PlundererEntity captain = ModEntities.PLUNDERER.get().create(level);
            if (captain != null) {
                raid.joinRaid(wave, captain, spawnPos, false);
                captains.add(captain);
            }
        }

        List<Boat> boats = new ArrayList<>();
        for (PlundererEntity captain : captains) {
            boats.add(spawnCaptainedBoat(level, raid, captain, spawnPos));
        }

        int nextFreeBoat = 0;
        for (Raider raider : crew) {
            if (nextFreeBoat < boats.size()) {
                raider.startRiding(boats.get(nextFreeBoat++), true);
            }
            ensureNavalGoals(raider);
        }
    }

    private static Boat spawnCaptainedBoat(ServerLevel level, Raid raid, PlundererEntity captain, BlockPos spawnPos) {
        BlockPos pos = pickBoatPos(level, spawnPos);
        Boat boat = new Boat(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        boat.setVariant(Boat.Type.DARK_OAK);
        boat.setYRot(yawTowards(pos, raid.getCenter()));
        level.addFreshEntity(boat);
        captain.startRiding(boat, true);
        return boat;
    }

    private static BlockPos pickBoatPos(ServerLevel level, BlockPos spawnPos) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < BOAT_POS_ATTEMPTS; attempt++) {
            BlockPos candidate = spawnPos.offset(
                    random.nextIntBetweenInclusive(-BOAT_SPREAD, BOAT_SPREAD), 0,
                    random.nextIntBetweenInclusive(-BOAT_SPREAD, BOAT_SPREAD));
            if (level.getBlockState(candidate).isAir() && isStillWater(level, candidate.below())) {
                return candidate;
            }
        }
        return spawnPos;
    }

    private static float yawTowards(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90;
    }

    private static void ensureNavalGoals(Mob mob) {
        if (!hasGoal(mob, AbandonShipGoal.class)) {
            mob.goalSelector.addGoal(ABANDON_SHIP_GOAL_PRIORITY, new AbandonShipGoal(mob));
        }
        if (!hasGoal(mob, BoardBoatGoal.class)) {
            mob.goalSelector.addGoal(BOARD_BOAT_GOAL_PRIORITY, new BoardBoatGoal(mob, 1, BOARD_BOAT_TRY_INTERVAL, true));
        }
    }

    private static boolean hasGoal(Mob mob, Class<? extends Goal> goalClass) {
        return mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(g -> goalClass.isInstance(g.getGoal()));
    }
}
