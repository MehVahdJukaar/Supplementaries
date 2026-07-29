package net.mehvahdjukaar.supplementaries.common.misc.cooperative;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedData;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedDataType;
import net.mehvahdjukaar.supplementaries.reg.ModData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

/**
 * Per-level cooperation tracker for pulley pulls, mirroring {@link PistonCooperationData}.
 * When two pulleys fire a continuous step on the same tick with the same period and push
 * direction, this is how they discover each other so the resolver runs once with a combined
 * PulleyInfo set, letting them pull a shared structure together (e.g. an elevator on two
 * ropes).
 * <p>
 * Persisted via Moonlight's {@code WorldSavedData} so the per-level instance is naturally
 * isolated and survives chunk lifecycle quirks (unlike a static map). Entries auto-purge
 * after {@link #MAX_AGE_TICKS}; the on-disk persistence is incidental, since the cooperation
 * window is single-tick in practice.
 * <p>
 * <b>Server-only instance.</b> Client uses the static {@link #markAttemptingClient} /
 * {@link #getCooperatorsClient} side-channel (same shape as the piston version) because the
 * client doesn't host a WorldSavedData. Both sides mark on the same local analog-driver
 * tick, so the matching {@code triggerEvent} (arriving on the client a few ticks late) sees
 * the same cooperator set on either side.
 */
public class PulleyCooperationData extends WorldSavedData {

    /**
     * Cooperator search radius per axis. Roughly matches the resolver's per-pulley budget.
     */
    private static final int MAX_DISTANCE = 12;
    /**
     * Attempt/consumed entries older than this many ticks are purged.
     */
    private static final int MAX_AGE_TICKS = 20;

    private record AttemptInfo(int period, Direction pushDir, long tick) {
        static final Codec<AttemptInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("period").forGetter(AttemptInfo::period),
                Direction.CODEC.fieldOf("dir").forGetter(AttemptInfo::pushDir),
                Codec.LONG.fieldOf("tick").forGetter(AttemptInfo::tick)
        ).apply(i, AttemptInfo::new));
    }

    // Map keys must round-trip as strings so this works under NbtOps (map keys become StringTag).
    private static final Codec<Long> POS_KEY_CODEC = Codec.STRING.xmap(Long::parseLong, String::valueOf);

    public static final Codec<PulleyCooperationData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(POS_KEY_CODEC, AttemptInfo.CODEC).fieldOf("pulleys").forGetter(d -> d.attempting)
    ).apply(i, PulleyCooperationData::new));

    private final Map<Long, AttemptInfo> attempting = new HashMap<>();
    private final Map<Long, Long> consumed = new HashMap<>();

    public PulleyCooperationData(ServerLevel level) {
    }

    private PulleyCooperationData(Map<Long, AttemptInfo> attempting) {
        this.attempting.putAll(attempting);
    }

    @Override
    public WorldSavedDataType<PulleyCooperationData> getType() {
        return ModData.COOPERATIVE_PULLEYS;
    }

    private void purgeOld(long currentTick) {
        attempting.entrySet().removeIf(e -> currentTick - e.getValue().tick() > MAX_AGE_TICKS);
        consumed.entrySet().removeIf(e -> currentTick - e.getValue() > MAX_AGE_TICKS);
    }

    public void markAttempting(BlockPos pos, int period, Direction pushDir, long tick) {
        purgeOld(tick);
        attempting.put(pos.asLong(), new AttemptInfo(period, pushDir, tick));
        setDirty();
    }

    public Set<BlockPos> getCooperators(BlockPos primary, int period, Direction pushDir, long currentTick) {
        return collectCooperators(attempting, consumed, primary, period, pushDir, currentTick);
    }

    public boolean wasConsumed(BlockPos pos, long currentTick) {
        return wasConsumedIn(consumed, pos, currentTick);
    }

    public void markConsumed(BlockPos pos, long tick) {
        purgeOld(tick);
        consumed.put(pos.asLong(), tick);
    }

    private static Set<BlockPos> collectCooperators(Map<Long, AttemptInfo> attempts, Map<Long, Long> consumedTicks,
                                                    BlockPos primary, int period, Direction pushDir, long currentTick) {
        if (attempts.size() <= 1) return Collections.emptySet();
        Set<BlockPos> result = new HashSet<>();
        for (Map.Entry<Long, AttemptInfo> entry : attempts.entrySet()) {
            AttemptInfo info = entry.getValue();
            if (info.pushDir() != pushDir) continue;
            if (info.period() != period) continue;
            if (currentTick - info.tick() > MAX_AGE_TICKS) continue;
            BlockPos candidate = BlockPos.of(entry.getKey());
            if (candidate.equals(primary)) continue;
            if (wasConsumedIn(consumedTicks, candidate, currentTick)) continue;
            if (outOfRange(candidate, primary)) continue;
            result.add(candidate);
        }
        return result;
    }

    // Same-tick only: "consumed" exists solely to dedupe a cooperator's own blockEvent within the
    // single runBlockEvents pass that already moved its chain. MAX_AGE_TICKS is the purge horizon,
    // NOT the validity window; using it here swallowed legitimate steps for ~20 ticks after every
    // move, stalling repeated input (e.g. crank spam).
    private static boolean wasConsumedIn(Map<Long, Long> consumedTicks, BlockPos pos, long currentTick) {
        Long consumedAt = consumedTicks.get(pos.asLong());
        return consumedAt != null && consumedAt == currentTick;
    }

    private static boolean outOfRange(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) > MAX_DISTANCE
                || Math.abs(a.getY() - b.getY()) > MAX_DISTANCE
                || Math.abs(a.getZ() - b.getZ()) > MAX_DISTANCE;
    }

    // -------------------------------------------------------------------------
    // Client side-channel. WorldSavedData is server-only; the client maintains its own
    // tick-scoped tables. Both sides mark on the same local tick (the analog driver runs
    // on both), so by the time a blockEvent's triggerEvent runs on the client it finds the
    // same cooperator set the server resolved with. Tick-reset prevents unbounded growth.
    // -------------------------------------------------------------------------
    private static long clientLastSeenTick = Long.MIN_VALUE;
    private static final Map<Long, AttemptInfo> clientAttempts = new HashMap<>();
    private static final Map<Long, Long> clientConsumed = new HashMap<>();

    private static void rolloverClient(long now) {
        if (now != clientLastSeenTick) {
            clientLastSeenTick = now;
            clientAttempts.entrySet().removeIf(e -> now - e.getValue().tick() > MAX_AGE_TICKS);
            clientConsumed.entrySet().removeIf(e -> now - e.getValue() > MAX_AGE_TICKS);
        }
    }

    public static void markAttemptingClient(BlockPos pos, int period, Direction pushDir, long tick) {
        rolloverClient(tick);
        clientAttempts.put(pos.asLong(), new AttemptInfo(period, pushDir, tick));
    }

    public static Set<BlockPos> getCooperatorsClient(BlockPos primary, int period, Direction pushDir, long currentTick) {
        return collectCooperators(clientAttempts, clientConsumed, primary, period, pushDir, currentTick);
    }

    public static boolean wasConsumedClient(BlockPos pos, long currentTick) {
        return wasConsumedIn(clientConsumed, pos, currentTick);
    }

    public static void markConsumedClient(BlockPos pos, long tick) {
        rolloverClient(tick);
        clientConsumed.put(pos.asLong(), tick);
    }
}
