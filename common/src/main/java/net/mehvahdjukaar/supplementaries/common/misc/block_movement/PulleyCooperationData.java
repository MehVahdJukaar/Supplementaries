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

// same as PistonCooperationData but for pulleys. lets an elevator hanging on two ropes move
public class PulleyCooperationData extends WorldSavedData {

    //search radius per axis
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
        //a chain that already moved this tick can't join a second resolve
        cooperators.removeIf(pos -> table.wasHandled(pos, currentTick));
        return cooperators;
    }

    //unlike pistons, same column is fine. stacked pulleys on one rope are legit
    private static boolean withinReach(BlockPos candidate, BlockPos primary) {
        return Math.abs(candidate.getX() - primary.getX()) <= MAX_DISTANCE
                && Math.abs(candidate.getY() - primary.getY()) <= MAX_DISTANCE
                && Math.abs(candidate.getZ() - primary.getZ()) <= MAX_DISTANCE;
    }

    //client copy, WorldSavedData is server only
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
