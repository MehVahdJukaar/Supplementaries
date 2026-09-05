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
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Set;

// lets an elevator hanging on two ropes move
public class PulleyCooperationData extends WorldSavedData {

    //search radius per axis
    private static final int MAX_DISTANCE = 12;

    //client mirror, WorldSavedData is server only. filled by ClientBoundPulleyAttemptPacket
    private static final CooperationTable<AttemptInfo> CLIENT_TABLE = new CooperationTable<>();

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

    public static void markAttemptingClient(BlockPos pos, int period, Direction pushDir, long tick) {
        CLIENT_TABLE.markAttempting(pos, new AttemptInfo(period, pushDir, tick));
    }

    public static Set<BlockPos> getCooperators(Level level, BlockPos primary, int period, Direction pushDir, long currentTick) {
        CooperationTable<AttemptInfo> table = tableFor(level);
        Set<BlockPos> cooperators = table.getCooperators(primary, currentTick, (candidate, attempt) ->
                attempt.pushDir() == pushDir && attempt.period() == period
                        && withinReach(candidate, primary));
        //a chain that already moved this tick can't join a second resolve
        cooperators.removeIf(pos -> table.wasHandled(pos, currentTick));
        return cooperators;
    }

    public static boolean wasConsumed(Level level, BlockPos pos, long currentTick) {
        return tableFor(level).wasHandled(pos, currentTick);
    }

    public static void markConsumed(Level level, BlockPos pos, long tick) {
        tableFor(level).markHandled(pos, tick);
    }

    private static CooperationTable<AttemptInfo> tableFor(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return ModData.COOPERATIVE_PULLEYS.getData(serverLevel).table;
        }
        return CLIENT_TABLE;
    }

    //unlike pistons, same column is fine. stacked pulleys on one rope are legit
    private static boolean withinReach(BlockPos candidate, BlockPos primary) {
        return Math.abs(candidate.getX() - primary.getX()) <= MAX_DISTANCE
                && Math.abs(candidate.getY() - primary.getY()) <= MAX_DISTANCE
                && Math.abs(candidate.getZ() - primary.getZ()) <= MAX_DISTANCE;
    }
}
