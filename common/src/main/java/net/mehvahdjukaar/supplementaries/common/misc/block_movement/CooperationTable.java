package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Bookkeeping shared by cooperative pistons and cooperative pulleys: who tried to move on which
 * tick, so a mover firing later in the same tick can find the others and resolve one structure for
 * the whole group, plus who has already been dealt with so their own event becomes a no-op.
 * <p>
 * Each feature owns two of these: one inside its per-level {@code WorldSavedData} and one static for
 * the client, which re-runs the move locally but cannot host saved data. Both need identical logic,
 * which is why it lives here rather than in either data class.
 * <p>
 * What a cooperator <i>is</i> stays with the caller, as a single predicate over candidate position
 * and attempt: pistons and pulleys key on different things and disagree about which neighbours can
 * plausibly share a structure. The table only owns what they agree on, which is expiry, not
 * cooperating with yourself, and the same-tick handled marker.
 *
 * @param <A> the per-position attempt record, whatever that feature needs to compare
 */
public class CooperationTable<A extends CooperationTable.Attempt> {

    /**
     * Entries older than this are dropped. The cooperation window is a single tick in practice; the
     * slack exists because a rescheduled {@code moveBlocks} can run a few ticks after the attempt
     * was registered, and the client's block event arrives later still.
     */
    private static final int MAX_AGE = 20;

    public interface Attempt {
        long tick();
    }

    private final Map<Long, A> attempts = new HashMap<>();
    // Transient in both features: only meaningful within the tick it was written, never persisted.
    private final Map<Long, Long> handled = new HashMap<>();

    public Map<Long, A> attempts() {
        return this.attempts;
    }

    public void markAttempting(BlockPos pos, A attempt) {
        purge(attempt.tick());
        this.attempts.put(pos.asLong(), attempt);
    }

    /**
     * Positions whose attempt is still fresh, isn't the caller's own, and which
     * {@code isCooperator} accepts. Mutable, so callers can apply further filtering.
     */
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

    /**
     * Same tick only. This exists to dedupe a mover's own event within the pass that already handled
     * it as part of someone else's group; {@link #MAX_AGE} is the purge horizon, NOT the validity
     * window. Using the latter here once swallowed legitimate moves for ~20 ticks afterwards,
     * stalling repeated input such as crank spam.
     */
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
