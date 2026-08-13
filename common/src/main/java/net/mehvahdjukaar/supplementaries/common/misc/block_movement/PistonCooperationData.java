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
import net.minecraft.world.level.block.piston.PistonStructureResolver;

import java.util.Map;
import java.util.Set;

// Which pistons are attempting to move this tick, so one that can't resolve a structure alone
// finds the neighbours pushing the same way and pools their budget. Per-level, not synced.
// Storage and expiry live in CooperationTable; this class defines what a piston attempt is and
// which neighbours count as reachable.
public class PistonCooperationData extends WorldSavedData {

    private record AttemptInfo(Direction direction, boolean extending, long tick) implements CooperationTable.Attempt {
        static final Codec<AttemptInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
                Direction.CODEC.fieldOf("dir").forGetter(AttemptInfo::direction),
                Codec.BOOL.fieldOf("ext").forGetter(AttemptInfo::extending),
                Codec.LONG.fieldOf("tick").forGetter(AttemptInfo::tick)
        ).apply(i, AttemptInfo::new));
    }

    public static final Codec<PistonCooperationData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(MiscUtils.LONG_STRING_CODEC, AttemptInfo.CODEC).fieldOf("pistons")
                    .forGetter(d -> d.table.attempts())
    ).apply(i, PistonCooperationData::new));

    private final CooperationTable<AttemptInfo> table = new CooperationTable<>();

    private PistonCooperationData(Map<Long, AttemptInfo> attempts) {
        this.table.attempts().putAll(attempts);
    }

    public PistonCooperationData(ServerLevel level) {
    }

    @Override
    public WorldSavedDataType<PistonCooperationData> getType() {
        return ModData.COOPERATIVE_PISTONS;
    }

    public void markAttempting(BlockPos pos, Direction dir, boolean extending, long tick) {
        this.table.markAttempting(pos, new AttemptInfo(dir, extending, tick));
        setDirty();
    }

    public Set<BlockPos> getCooperators(BlockPos pistonPos, Direction dir, boolean extending, long currentTick) {
        return cooperatorsIn(this.table, pistonPos, dir, extending, currentTick);
    }

    // Whether this piston's block event was already posted this tick, as part of a group move.
    public boolean hasPosted(BlockPos pos, long tick) {
        return this.table.wasHandled(pos, tick);
    }

    public void markPosted(BlockPos pos, long tick) {
        this.table.markHandled(pos, tick);
    }

    private static Set<BlockPos> cooperatorsIn(CooperationTable<AttemptInfo> table, BlockPos pistonPos,
                                               Direction dir, boolean extending, long currentTick) {
        return table.getCooperators(pistonPos, currentTick, (candidate, attempt) ->
                attempt.direction() == dir && attempt.extending() == extending
                        && canReachSameStructure(candidate, pistonPos, dir.getAxis()));
    }

    // A cooperator sits in a different column, close enough that one structure could span the two.
    // MAX_PUSH_DEPTH on purpose, not the configured limit: it bounds how far a structure can reach.
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

    // Side-channel for the client-side moveBlocks animation, which re-runs the resolve and needs
    // the same cooperative limit. Purely cosmetic: correctness is owned by the saved data above.
    private static final CooperationTable<AttemptInfo> CLIENT_TABLE = new CooperationTable<>();

    public static void markAttemptingClient(BlockPos pos, Direction dir, boolean extending, long tick) {
        CLIENT_TABLE.markAttempting(pos, new AttemptInfo(dir, extending, tick));
    }

    public static Set<BlockPos> getCooperatorsClient(BlockPos pistonPos, Direction dir, boolean extending,
                                                     long currentTick) {
        return cooperatorsIn(CLIENT_TABLE, pistonPos, dir, extending, currentTick);
    }
}
