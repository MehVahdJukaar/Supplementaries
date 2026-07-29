package net.mehvahdjukaar.supplementaries.common.misc.cooperative;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedData;
import net.mehvahdjukaar.moonlight.api.misc.WorldSavedDataType;
import net.mehvahdjukaar.supplementaries.common.utils.MiscUtils;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
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
public class PistonCooperationData extends WorldSavedData {

    private static final int MAX_AGE = 20;

    /**
     * Whether our own capture/restore runs for piston moves.
     * <p>
     * Quark's <i>pistons move tile entities</i> module does the same job with its own pipeline, and
     * the two cannot share a move: both strip the source block entity and both rebuild one at the
     * destination, so whichever writes last wins and the loser's data is silently gone (only blocks
     * on Quark's delayed-update list survive, since those are written a tick later). Quark detaches
     * at the very top of {@code moveBlocks} and so always captures first, leaving ours empty.
     * Rather than race, we hand pistons over entirely whenever that module is on.
     * <p>
     * Pulleys keep our pipeline either way: Quark only hooks {@code PistonBaseBlock.moveBlocks} and
     * {@code PistonMovingBlockEntity.tick}, neither of which a pulley move goes through, so there
     * is nothing to defer to there.
     */
    public static boolean blockEntityMovesHandledByUs() {
        return CommonConfigs.Tweaks.PUSH_BLOCK_ENTITIES.get() && !blockEntityMovesHandledByQuark();
    }

    /**
     * Whether Quark's module is present and enabled, and therefore owns piston moves.
     */
    public static boolean blockEntityMovesHandledByQuark() {
        return CompatHandler.QUARK && QuarkCompat.isMovingTileEntitiesEnabled();
    }

    private record AttemptInfo(Direction direction, boolean extending, long tick) {
        static final Codec<AttemptInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
                Direction.CODEC.fieldOf("dir").forGetter(AttemptInfo::direction),
                Codec.BOOL.fieldOf("ext").forGetter(AttemptInfo::extending),
                Codec.LONG.fieldOf("tick").forGetter(AttemptInfo::tick)
        ).apply(i, AttemptInfo::new));
    }

    public static final Codec<PistonCooperationData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(MiscUtils.LONG_STRING_CODEC, AttemptInfo.CODEC).fieldOf("pistons").forGetter(d -> d.attemptingPistons)
    ).apply(i, PistonCooperationData::new));

    private final Map<Long, AttemptInfo> attemptingPistons = new HashMap<>();
    // Transient: only used within a single tick to dedup event posting, not persisted.
    private final Map<Long, Long> postedPistons = new HashMap<>();

    private PistonCooperationData(Map<Long, AttemptInfo> attemptingPistons) {
        this.attemptingPistons.putAll(attemptingPistons);
    }

    public PistonCooperationData(ServerLevel level) {
    }

    @Override
    public WorldSavedDataType<PistonCooperationData> getType() {
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
        return postedTick != null && postedTick == tick;
    }

    public void markPosted(BlockPos pos, long tick) {
        postedPistons.put(pos.asLong(), tick);
    }

    /**
     * Returns cooperating piston positions from this level's tracker.
     * Accepts entries from any tick within MAX_AGE of currentTick so that a
     * rescheduled moveBlocks can still find its cooperation group.
     */
    public Set<BlockPos> getCooperators(BlockPos pistonPos, Direction dir, boolean extending, long currentTick) {
        return collectCooperators(attemptingPistons, pistonPos, dir, extending, currentTick);
    }

    private static Set<BlockPos> collectCooperators(Map<Long, AttemptInfo> attempts, BlockPos pistonPos,
                                                    Direction dir, boolean extending, long currentTick) {
        if (attempts.size() <= 1) return Collections.emptySet();

        Set<BlockPos> cooperators = new HashSet<>();
        for (Map.Entry<Long, AttemptInfo> entry : attempts.entrySet()) {
            AttemptInfo info = entry.getValue();
            if (!dir.equals(info.direction())) continue;
            if (extending != info.extending()) continue;
            if (currentTick - info.tick() > MAX_AGE) continue;

            BlockPos candidate = BlockPos.of(entry.getKey());
            if (candidate.equals(pistonPos)) continue;
            if (canReachSameStructure(candidate, pistonPos, dir.getAxis())) cooperators.add(candidate);
        }
        return cooperators;
    }

    // A cooperator has to sit in a different column (nonzero perpendicular offset) and be close
    // enough both along and across the push axis that one structure could plausibly span the two.
    private static boolean canReachSameStructure(BlockPos candidate, BlockPos pistonPos, Direction.Axis pushAxis) {
        int dx = candidate.getX() - pistonPos.getX();
        int dy = candidate.getY() - pistonPos.getY();
        int dz = candidate.getZ() - pistonPos.getZ();

        int pushOffset = Math.abs(pushAxis.choose(dx, dy, dz));
        if (pushOffset >= PistonStructureResolver.MAX_PUSH_DEPTH) return false;

        int perpX = pushAxis == Direction.Axis.X ? 0 : dx;
        int perpY = pushAxis == Direction.Axis.Y ? 0 : dy;
        int perpZ = pushAxis == Direction.Axis.Z ? 0 : dz;
        if (perpX == 0 && perpY == 0 && perpZ == 0) return false;
        int perpDist = Math.max(Math.abs(perpX), Math.max(Math.abs(perpY), Math.abs(perpZ)));
        return perpDist <= PistonStructureResolver.MAX_PUSH_DEPTH;
    }

    // -------------------------------------------------------------------------
    // Static side-channel for client-side moveBlocks animation.
    //
    // checkIfExtend is server-only, but in an integrated server both client and
    // server share the same JVM. The server thread writes here; the client thread
    // reads it in supp$gateCoopOnResolve to apply the same cooperative limit,
    // giving smooth piston animations. Intentionally tick-reset and not per-level
    // (purely cosmetic: correctness is owned by the per-level WorldSavedData).
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
        // Every client entry was registered on clientTick, so the age check is a no-op here.
        return collectCooperators(clientAttempts, pistonPos, dir, extending, clientTick);
    }
}
