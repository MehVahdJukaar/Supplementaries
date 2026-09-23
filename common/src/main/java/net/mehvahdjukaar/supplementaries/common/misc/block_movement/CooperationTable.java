package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

// keeps track of who tried to move on which tick
public class CooperationTable<A extends CooperationTable.Attempt> {

    private static final int MAX_AGE = 20;

    public interface Attempt {
        long tick();

        default boolean isStale(long currentTick) {
            return Math.abs(currentTick - tick()) > MAX_AGE;
        }
    }

    private final Map<Long, A> attempts = new ConcurrentHashMap<>();
    private final Map<Long, Long> movedTickByPos = new ConcurrentHashMap<>();

    public Map<Long, A> attempts() {
        return this.attempts;
    }

    public void markAttempting(BlockPos pos, A attempt) {
        removeStale(attempt.tick());
        this.attempts.put(pos.asLong(), attempt);
    }

    public Set<BlockPos> getCooperators(BlockPos primary, long currentTick, BiPredicate<BlockPos, A> isCooperator) {
        Set<BlockPos> cooperators = new HashSet<>();
        if (this.attempts.size() <= 1) return cooperators;
        for (Map.Entry<Long, A> entry : this.attempts.entrySet()) {
            A attempt = entry.getValue();
            if (attempt.isStale(currentTick)) continue;
            BlockPos candidate = BlockPos.of(entry.getKey());
            if (candidate.equals(primary)) continue;
            if (!isCooperator.test(candidate, attempt)) continue;
            cooperators.add(candidate);
        }
        return cooperators;
    }

    //same tick only. using MAX_AGE here stalls repeated input like crank spam
    public boolean wasMovedThisTick(BlockPos pos, long currentTick) {
        Long tick = this.movedTickByPos.get(pos.asLong());
        return tick != null && tick == currentTick;
    }

    public void markMoved(BlockPos pos, long tick) {
        removeStale(tick);
        this.movedTickByPos.put(pos.asLong(), tick);
    }

    private void removeStale(long currentTick) {
        this.attempts.entrySet().removeIf(e -> e.getValue().isStale(currentTick));
        this.movedTickByPos.entrySet().removeIf(e -> e.getValue().isStale(currentTick));
    }

}
