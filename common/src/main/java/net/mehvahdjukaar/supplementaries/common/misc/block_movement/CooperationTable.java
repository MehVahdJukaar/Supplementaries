package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

// Bookkeeping shared by cooperative pistons and cooperative pulleys: who tried to move on which
// tick, so a mover firing later in the same tick finds the others and resolves one structure for
// the whole group, plus who was already dealt with so their own event becomes a no-op.
// Each feature owns two of these, one in its per-level WorldSavedData and one static for the client
// which re-runs the move locally but can't host saved data. Same logic both sides, hence living here.
// What counts as a cooperator stays with the caller as a predicate over position and attempt (A):
// pistons and pulleys disagree about which neighbours could plausibly share a structure. The table
// owns only what they agree on: expiry, not cooperating with yourself, the same-tick handled marker.
public class CooperationTable<A extends CooperationTable.Attempt> {

    // Purge horizon. Cooperation is a single tick in practice; the slack covers a rescheduled
    // moveBlocks running a few ticks after the attempt, and the client's later block event.
    private static final int MAX_AGE = 20;

    public interface Attempt {
        long tick();
    }

    // Concurrent because the client side-channel tables are written by the integrated server
    // thread and read by the client thread. Plain HashMaps here crashed with a CME.
    private final Map<Long, A> attempts = new ConcurrentHashMap<>();
    // Transient in both features: only meaningful within the tick it was written, never persisted.
    private final Map<Long, Long> handled = new ConcurrentHashMap<>();

    public Map<Long, A> attempts() {
        return this.attempts;
    }

    public void markAttempting(BlockPos pos, A attempt) {
        purge(attempt.tick());
        this.attempts.put(pos.asLong(), attempt);
    }

    // Fresh attempts other than the caller's own that isCooperator accepts. Mutable, so callers
    // can filter further.
    public Set<BlockPos> getCooperators(BlockPos primary, long currentTick, BiPredicate<BlockPos, A> isCooperator) {
        Set<BlockPos> cooperators = new HashSet<>();
        if (this.attempts.size() <= 1) return cooperators;
        for (Map.Entry<Long, A> entry : this.attempts.entrySet()) {
            A attempt = entry.getValue();
            if (currentTick - attempt.tick() > MAX_AGE) continue;
            BlockPos candidate = BlockPos.of(entry.getKey());
            if (candidate.equals(primary)) continue;
            if (!isCooperator.test(candidate, attempt)) continue;
            cooperators.add(candidate);
        }
        return cooperators;
    }

    // Same tick only: this dedupes a mover's own event against the pass that already handled it as
    // part of someone else's group. MAX_AGE is the purge horizon, not a validity window; using it
    // here once swallowed legitimate moves for ~20 ticks, stalling repeated input like crank spam.
    public boolean wasHandled(BlockPos pos, long currentTick) {
        Long tick = this.handled.get(pos.asLong());
        return tick != null && tick == currentTick;
    }

    public void markHandled(BlockPos pos, long tick) {
        purge(tick);
        this.handled.put(pos.asLong(), tick);
    }

    private void purge(long currentTick) {
        this.attempts.entrySet().removeIf(e -> currentTick - e.getValue().tick() > MAX_AGE);
        this.handled.entrySet().removeIf(e -> currentTick - e.getValue() > MAX_AGE);
    }
}
