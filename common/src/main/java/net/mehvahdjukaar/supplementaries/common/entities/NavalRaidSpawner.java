package net.mehvahdjukaar.supplementaries.common.entities;

import net.mehvahdjukaar.supplementaries.common.entities.goals.AbandonShipGoal;
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
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class NavalRaidSpawner {

    //same ring vanilla uses for its first land attempts
    private static final int RING_RADIUS = 64;
    private static final int RING_ATTEMPTS = 24;
    private static final int BOAT_SPREAD = 4;
    private static final int BOAT_POS_ATTEMPTS = 8;
    private static final int ABANDON_SHIP_GOAL_PRIORITY = 3;

    @Nullable
    public static BlockPos findWaterSpawnPosWhenNoLand(ServerLevel level, BlockPos center) {
        if (!CommonConfigs.Functional.PLUNDERER_ENABLED.get()) return null;
        if (!CommonConfigs.Functional.NAVAL_RAID_WHEN_NO_LAND.get()) return null;
        for (int radius = 8; radius <= RING_RADIUS; radius *= 2) {
            BlockPos water = findOpenWaterAround(level, center, radius);
            if (water != null) return water;
        }
        return null;
    }

    public static void spawnNavalDivision(ServerLevel level, Raid raid, int wave, BlockPos spawnPos, @Nullable Set<Raider> waveRaiders) {
        if (waveRaiders == null || !CommonConfigs.Functional.PLUNDERER_ENABLED.get()) return;
        Raider leader = raid.getLeader(wave);
        List<Raider> pillagers = new ArrayList<>();
        List<Raider> others = new ArrayList<>();
        for (Raider raider : waveRaiders) {
            if (raider.isPassenger()) continue; //ravager riders stay on their ravager
            if (raider.getType() == EntityType.PILLAGER && raider != leader) pillagers.add(raider);
            else others.add(raider);
        }

        boolean spawnedOnWater = level.getFluidState(spawnPos.below()).is(FluidTags.WATER);
        if (spawnedOnWater) {
            int total = pillagers.size() + others.size();
            int helms = Math.min(pillagers.size(), (total + 1) / 2);
            List<Raider> passengers = new ArrayList<>(others);
            passengers.addAll(pillagers.subList(helms, pillagers.size()));
            launchBoats(level, raid, wave, spawnPos, pillagers.subList(0, helms), passengers, true);
        } else if (level.random.nextFloat() < CommonConfigs.Functional.NAVAL_RAID_CHANCE.get()) {
            int boats = Math.min(boatsForWave(level, raid, wave), (pillagers.size() + 1) / 2);
            if (boats == 0) return;
            BlockPos water = findOpenWaterAround(level, raid.getCenter(), RING_RADIUS);
            if (water == null) return;
            List<Raider> passengers = pillagers.subList(boats, Math.min(boats * 2, pillagers.size()));
            launchBoats(level, raid, wave, water, pillagers.subList(0, boats), passengers, false);
        }
    }

    private static void launchBoats(ServerLevel level, Raid raid, int wave, BlockPos water,
                                    List<Raider> replaced, List<Raider> passengers, boolean addHelmsmenIfNeeded) {
        Iterator<Raider> toBoard = passengers.iterator();
        for (Raider pillager : replaced) {
            Boat boat = spawnBoat(level, raid, water);
            PlundererEntity helmsman = ModEntities.PLUNDERER.get().create(level);
            if (helmsman == null) {
                boat.discard();
                return;
            }
            raid.removeFromRaid(pillager, true);
            pillager.discard();
            raid.joinRaid(wave, helmsman, boat.blockPosition(), false);
            helmsman.startRiding(boat, true);
            if (toBoard.hasNext()) board(toBoard.next(), boat);
        }
        while (addHelmsmenIfNeeded && toBoard.hasNext()) {
            Boat boat = spawnBoat(level, raid, water);
            PlundererEntity helmsman = ModEntities.PLUNDERER.get().create(level);
            if (helmsman == null) {
                boat.discard();
                return;
            }
            raid.joinRaid(wave, helmsman, boat.blockPosition(), false);
            helmsman.startRiding(boat, true);
            board(toBoard.next(), boat);
        }
    }

    private static Boat spawnBoat(ServerLevel level, Raid raid, BlockPos water) {
        BlockPos pos = pickBoatPos(level, water);
        Boat boat = new Boat(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        boat.setVariant(Boat.Type.DARK_OAK);
        boat.setYRot(yawTowards(pos, raid.getCenter()));
        level.addFreshEntity(boat);
        return boat;
    }

    private static void board(Raider passenger, Boat boat) {
        passenger.getNavigation().stop();
        passenger.moveTo(boat.getX(), boat.getY(), boat.getZ());
        passenger.startRiding(boat, true);
        if (!hasGoal(passenger, AbandonShipGoal.class)) {
            passenger.goalSelector.addGoal(ABANDON_SHIP_GOAL_PRIORITY, new AbandonShipGoal(passenger));
        }
    }

    private static int boatsForWave(ServerLevel level, Raid raid, int wave) {
        int min = CommonConfigs.Functional.NAVAL_RAID_MIN_BOATS.get();
        int max = CommonConfigs.Functional.NAVAL_RAID_MAX_BOATS.get();
        if (max <= min) return min;
        int lastWave = raid.getNumGroups(level.getDifficulty());
        float progress = lastWave <= 1 ? 1 : Mth.clamp((wave - 1) / (float) (lastWave - 1), 0, 1);
        return min + Math.round((max - min) * progress);
    }

    @Nullable
    private static BlockPos findOpenWaterAround(ServerLevel level, BlockPos center, int radius) {
        RandomSource random = level.random;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < RING_ATTEMPTS; attempt++) {
            float angle = random.nextFloat() * Mth.TWO_PI;
            int x = center.getX() + Mth.floor(Mth.cos(angle) * radius) + random.nextInt(5);
            int z = center.getZ() + Mth.floor(Mth.sin(angle) * radius) + random.nextInt(5);
            pos.set(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z);
            boolean loaded = level.hasChunksAt(x - 10, z - 10, x + 10, z + 10) && level.isPositionEntityTicking(pos);
            if (loaded && isOpenWater(level, pos)) return pos.immutable();
        }
        return null;
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

    private static BlockPos pickBoatPos(ServerLevel level, BlockPos waterPos) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < BOAT_POS_ATTEMPTS; attempt++) {
            BlockPos candidate = waterPos.offset(
                    random.nextIntBetweenInclusive(-BOAT_SPREAD, BOAT_SPREAD), 0,
                    random.nextIntBetweenInclusive(-BOAT_SPREAD, BOAT_SPREAD));
            if (level.getBlockState(candidate).isAir() && isStillWater(level, candidate.below())) {
                return candidate;
            }
        }
        return waterPos;
    }

    private static float yawTowards(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90;
    }

    private static boolean hasGoal(Mob mob, Class<? extends Goal> goalClass) {
        return mob.goalSelector.getAvailableGoals().stream()
                .anyMatch(g -> goalClass.isInstance(g.getGoal()));
    }
}
