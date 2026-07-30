package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedData;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedDataType;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.reg.ModData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.Map;
import java.util.Set;

/**
 * Which pulleys are attempting a step this tick, mirroring {@link PistonCooperationData}. When two
 * pulleys fire a continuous step on the same tick with the same period and push direction, this is
 * how they discover each other so the resolver runs once with a combined PulleyInfo set, letting
 * them pull a shared structure together (e.g. an elevator on two ropes).
 * <p>
 * Storage, expiry and the same-tick "already handled" marker come from {@link CooperationTable};
 * this class supplies what a pulley attempt consists of and how far apart two of them may be.
 * <p>
 * <b>Server-only instance.</b> The client uses the static {@link #markAttemptingClient} /
 * {@link #getCooperatorsClient} side-channel (same shape as the piston version) because it can't
 * host a WorldSavedData. Both sides mark on the same local analog-driver tick, so the matching
 * {@code triggerEvent} (arriving on the client a few ticks late) sees the same cooperator set on
 * either side.
 */
public class PulleyCooperationData extends WorldSavedData {

    /**
     * Cooperator search radius per axis. Roughly matches the resolver's per-pulley budget.
     */
    private static final int MAX_DISTANCE = 12;

    private record AttemptInfo(int period, Direction pushDir, long tick) implements CooperationTable.Attempt {
        static final Codec<AttemptInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("period").forGetter(AttemptInfo::period),
                Direction.CODEC.fieldOf("dir").forGetter(AttemptInfo::pushDir),
                Codec.LONG.fieldOf("tick").forGetter(AttemptInfo::tick)
        ).apply(i, AttemptInfo::new));
    }

    public static final Codec<PulleyCooperationData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(MiscUtils.LONG_STRING_CODEC, AttemptInfo.CODEC).fieldOf("pulleys")
                    .forGetter(d -> d.table.attempts())
    ).apply(i, PulleyCooperationData::new));

    private final CooperationTable<AttemptInfo> table = new CooperationTable<>();

    private PulleyCooperationData(Map<Long, AttemptInfo> attempts) {
        this.table.attempts().putAll(attempts);
    }

    public PulleyCooperationData(ServerLevel level) {
    }

    @Override
    public WorldSavedDataType<PulleyCooperationData> getType() {
        return ModData.COOPERATIVE_PULLEYS;
    }

    public void markAttempting(BlockPos pos, int period, Direction pushDir, long tick) {
        this.table.markAttempting(pos, new AttemptInfo(period, pushDir, tick));
        setDirty();
    }

    public Set<BlockPos> getCooperators(BlockPos primary, int period, Direction pushDir, long currentTick) {
        return cooperatorsIn(this.table, primary, period, pushDir, currentTick);
    }

    /** Whether this pulley's chain was already shifted this tick, as part of a group move. */
    public boolean wasConsumed(BlockPos pos, long currentTick) {
        return this.table.wasHandled(pos, currentTick);
    }

    public void markConsumed(BlockPos pos, long tick) {
        this.table.markHandled(pos, tick);
    }

    private static Set<BlockPos> cooperatorsIn(CooperationTable<AttemptInfo> table, BlockPos primary,
                                               int period, Direction pushDir, long currentTick) {
        Set<BlockPos> cooperators = table.getCooperators(primary, currentTick, (candidate, attempt) ->
                attempt.pushDir() == pushDir && attempt.period() == period
                        && withinReach(candidate, primary));
        // A pulley whose chain already moved this tick can't join a second resolve. The piston
        // equivalent doesn't filter here, because there the marker only tracks event posting.
        cooperators.removeIf(pos -> table.wasHandled(pos, currentTick));
        return cooperators;
    }

    /**
     * Plain box around the primary. Unlike the piston test this permits a cooperator in the same
     * column, since stacked pulleys winding the same rope are a legitimate setup and the resolver
     * treats every other pulley body as a wall anyway.
     */
    private static boolean withinReach(BlockPos candidate, BlockPos primary) {
        return Math.abs(candidate.getX() - primary.getX()) <= MAX_DISTANCE
                && Math.abs(candidate.getY() - primary.getY()) <= MAX_DISTANCE
                && Math.abs(candidate.getZ() - primary.getZ()) <= MAX_DISTANCE;
    }

    // -------------------------------------------------------------------------
    // Client side-channel. WorldSavedData is server-only; the client keeps its own table. Both
    // sides mark on the same local tick (the analog driver runs on both), so by the time a
    // blockEvent's triggerEvent runs on the client it finds the same cooperator set the server
    // resolved with.
    // -------------------------------------------------------------------------
    private static final CooperationTable<AttemptInfo> CLIENT_TABLE = new CooperationTable<>();

    public static void markAttemptingClient(BlockPos pos, int period, Direction pushDir, long tick) {
        CLIENT_TABLE.markAttempting(pos, new AttemptInfo(period, pushDir, tick));
    }

    public static Set<BlockPos> getCooperatorsClient(BlockPos primary, int period, Direction pushDir,
                                                     long currentTick) {
        return cooperatorsIn(CLIENT_TABLE, primary, period, pushDir, currentTick);
    }

    public static boolean wasConsumedClient(BlockPos pos, long currentTick) {
        return CLIENT_TABLE.wasHandled(pos, currentTick);
    }

    public static void markConsumedClient(BlockPos pos, long tick) {
        CLIENT_TABLE.markHandled(pos, tick);
    }
}
