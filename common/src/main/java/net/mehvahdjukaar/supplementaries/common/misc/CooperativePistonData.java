package net.mehvahdjukaar.supplementaries.common.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedData;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedDataType;
import net.mehvahdjukaar.supplementaries.reg.ModData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.piston.PistonStructureResolver;

import java.util.*;

/**
 * Per-level persistent replacement for PistonCooperationTracker.
 * Stored as WorldSavedData (per level, not synced) so entries survive world saves
 * and dimension boundaries are naturally isolated.
 * <p>
 * Entries carry their registration tick and expire after MAX_AGE ticks, which lets a
 * rescheduled moveBlocks (running a few ticks after checkIfExtend) still find its cooperators.
 */
public class CooperativePistonData extends WorldSavedData {

    private static final int MAX_AGE = 20;

    private record AttemptInfo(Direction direction, boolean extending, long tick) {
    }

    private record StoredEntry(long pos, Direction direction, boolean extending, long tick) {
        static final Codec<StoredEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.LONG.fieldOf("pos").forGetter(StoredEntry::pos),
                Direction.CODEC.fieldOf("dir").forGetter(StoredEntry::direction),
                Codec.BOOL.fieldOf("ext").forGetter(StoredEntry::extending),
                Codec.LONG.fieldOf("tick").forGetter(StoredEntry::tick)
        ).apply(i, StoredEntry::new));
    }

    // Root codec must produce a compound tag (not a bare list) so Moonlight's WorldSavedData.save()
    // can merge into an existing CompoundTag without hitting "mergeToList called with not a list".
    public static final Codec<CooperativePistonData> CODEC = RecordCodecBuilder.create(i -> i.group(
            StoredEntry.CODEC.listOf().fieldOf("pistons").forGetter(CooperativePistonData::toList)
    ).apply(i, CooperativePistonData::fromList));

    private final Map<Long, AttemptInfo> attemptingPistons = new HashMap<>();
    // transient — only used within a single tick to dedup event posting, not persisted
    private final Map<Long, Long> postedPistons = new HashMap<>();

    public CooperativePistonData() {
    }

    private static CooperativePistonData fromList(List<StoredEntry> list) {
        CooperativePistonData data = new CooperativePistonData();
        for (StoredEntry e : list) {
            data.attemptingPistons.put(e.pos(), new AttemptInfo(e.direction(), e.extending(), e.tick()));
        }
        return data;
    }

    private List<StoredEntry> toList() {
        return attemptingPistons.entrySet().stream()
                .map(e -> new StoredEntry(e.getKey(), e.getValue().direction(), e.getValue().extending(), e.getValue().tick()))
                .toList();
    }

    public static CooperativePistonData createFromLevel(ServerLevel level) {
        return new CooperativePistonData();
    }

    @Override
    public WorldSavedDataType<CooperativePistonData> getType() {
        return ModData.COOPERATIVE_PISTONS;
    }

    private void purgeOldEntries(long currentTick) {
        attemptingPistons.entrySet().removeIf(e -> currentTick - e.getValue().tick() > MAX_AGE);
        postedPistons.entrySet().removeIf(e -> currentTick - e.getValue() > MAX_AGE);
    }

    public void markAttempting(BlockPos pos, Direction dir, boolean extending, long tick) {
        purgeOldEntries(tick);
        attemptingPistons.put(pos.asLong(), new AttemptInfo(dir, extending, tick));
        setDirty();
    }

    public boolean hasPosted(BlockPos pos, long tick) {
        Long postedTick = postedPistons.get(pos.asLong());
        return postedTick != null && postedTick.longValue() == tick;
    }

    public void markPosted(BlockPos pos, long tick) {
        postedPistons.put(pos.asLong(), tick);
    }

    // -------------------------------------------------------------------------
    // Static side-channel for client-side moveBlocks animation.
    //
    // checkIfExtend is server-only, but in an integrated server both client and
    // server share the same JVM. The server thread writes here; the client thread
    // reads it in supp$wrapMoveBlocksResolver to apply the same cooperative limit,
    // giving smooth piston animations. Intentionally tick-reset and not per-level
    // (purely cosmetic — correctness is owned by the per-level WorldSavedData).
    // -------------------------------------------------------------------------
    private static long clientTick = Long.MIN_VALUE;
    private static final Map<Long, AttemptInfo> clientAttempts = new HashMap<>();

    public static void markAttemptingClient(BlockPos pos, Direction dir, boolean extending, long tick) {
        if (tick != clientTick) {
            clientTick = tick;
            clientAttempts.clear();
        }
        clientAttempts.put(pos.asLong(), new AttemptInfo(dir, extending, tick));
    }

    public static Set<BlockPos> getCooperatorsClient(BlockPos pistonPos, Direction dir, boolean extending) {
        if (clientAttempts.size() <= 1) return Collections.emptySet();

        Set<BlockPos> cooperators = new HashSet<>();
        Direction.Axis pushAxis = dir.getAxis();

        for (Map.Entry<Long, AttemptInfo> entry : clientAttempts.entrySet()) {
            AttemptInfo info = entry.getValue();
            if (!dir.equals(info.direction())) continue;
            if (extending != info.extending()) continue;

            BlockPos candidate = BlockPos.of(entry.getKey());
            if (candidate.equals(pistonPos)) continue;

            int dx = candidate.getX() - pistonPos.getX();
            int dy = candidate.getY() - pistonPos.getY();
            int dz = candidate.getZ() - pistonPos.getZ();

            int pushOffset = Math.abs(pushAxis == Direction.Axis.X ? dx :
                    pushAxis == Direction.Axis.Y ? dy : dz);
            if (pushOffset >= PistonStructureResolver.MAX_PUSH_DEPTH) continue;

            int perpX = pushAxis == Direction.Axis.X ? 0 : dx;
            int perpY = pushAxis == Direction.Axis.Y ? 0 : dy;
            int perpZ = pushAxis == Direction.Axis.Z ? 0 : dz;
            if (perpX == 0 && perpY == 0 && perpZ == 0) continue;
            int perpDist = Math.max(Math.abs(perpX), Math.max(Math.abs(perpY), Math.abs(perpZ)));
            if (perpDist > PistonStructureResolver.MAX_PUSH_DEPTH) continue;

            cooperators.add(candidate);
        }
        return cooperators;
    }

    /**
     * Returns cooperating piston positions from this level's tracker.
     * Accepts entries from any tick within MAX_AGE of currentTick so that a
     * rescheduled moveBlocks can still find its cooperation group.
     */
    public Set<BlockPos> getCooperators(BlockPos pistonPos, Direction dir, boolean extending, long currentTick) {
        if (attemptingPistons.size() <= 1) return Collections.emptySet();

        Set<BlockPos> cooperators = new HashSet<>();
        Direction.Axis pushAxis = dir.getAxis();

        for (Map.Entry<Long, AttemptInfo> entry : attemptingPistons.entrySet()) {
            AttemptInfo info = entry.getValue();
            if (!dir.equals(info.direction())) continue;
            if (extending != info.extending()) continue;
            if (currentTick - info.tick() > MAX_AGE) continue;

            BlockPos candidate = BlockPos.of(entry.getKey());
            if (candidate.equals(pistonPos)) continue;

            int dx = candidate.getX() - pistonPos.getX();
            int dy = candidate.getY() - pistonPos.getY();
            int dz = candidate.getZ() - pistonPos.getZ();

            int pushOffset = Math.abs(pushAxis == Direction.Axis.X ? dx :
                    pushAxis == Direction.Axis.Y ? dy : dz);
            if (pushOffset >= PistonStructureResolver.MAX_PUSH_DEPTH) continue;

            int perpX = pushAxis == Direction.Axis.X ? 0 : dx;
            int perpY = pushAxis == Direction.Axis.Y ? 0 : dy;
            int perpZ = pushAxis == Direction.Axis.Z ? 0 : dz;
            if (perpX == 0 && perpY == 0 && perpZ == 0) continue;
            int perpDist = Math.max(Math.abs(perpX), Math.max(Math.abs(perpY), Math.abs(perpZ)));
            if (perpDist > PistonStructureResolver.MAX_PUSH_DEPTH) continue;

            cooperators.add(candidate);
        }
        return cooperators;
    }
}
