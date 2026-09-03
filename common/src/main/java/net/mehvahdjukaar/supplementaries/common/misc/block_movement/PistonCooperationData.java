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
import net.minecraft.world.level.block.piston.PistonStructureResolver;

import java.util.Map;
import java.util.Set;

// pistons attempting to move this tick, so one that can't push alone can pool budget with the
// neighbours pushing the same way. per level, not synced
public class PistonCooperationData extends WorldSavedData {

    //animation only
    private static final CooperationTable<AttemptInfo> CLIENT_TABLE = new CooperationTable<>();

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

    public static PistonCooperationData get(ServerLevel level) {
        return ModData.COOPERATIVE_PISTONS.getData(level);
    }

    public void markAttempting(BlockPos pos, Direction dir, boolean extending, long tick) {
        AttemptInfo attempt = new AttemptInfo(dir, extending, tick);
        this.table.markAttempting(pos, attempt);
        CLIENT_TABLE.markAttempting(pos, attempt);
        setDirty();
    }

    public static Set<BlockPos> getCooperators(Level level, BlockPos pistonPos, Direction dir, boolean extending, long currentTick) {
        CooperationTable<AttemptInfo> table = level instanceof ServerLevel serverLevel ? get(serverLevel).table : CLIENT_TABLE;
        return table.getCooperators(pistonPos, currentTick, (candidate, attempt) ->
                attempt.direction() == dir && attempt.extending() == extending
                        && canReachSameStructure(candidate, pistonPos, dir.getAxis()));
    }

    public boolean hasPosted(BlockPos pos, long tick) {
        return this.table.wasHandled(pos, tick);
    }

    public void markPosted(BlockPos pos, long tick) {
        this.table.markHandled(pos, tick);
    }

    //MAX_PUSH_DEPTH and not the configured limit, this is how far a structure can reach
    private static boolean canReachSameStructure(BlockPos candidate, BlockPos pistonPos, Direction.Axis pushAxis) {
        int dx = candidate.getX() - pistonPos.getX();
        int dy = candidate.getY() - pistonPos.getY();
        int dz = candidate.getZ() - pistonPos.getZ();

        int pushOffset = Math.abs(pushAxis.choose(dx, dy, dz));
        if (pushOffset >= PistonStructureResolver.MAX_PUSH_DEPTH) return false;

        int targetX = pushAxis == Direction.Axis.X ? 0 : dx;
        int targetY = pushAxis == Direction.Axis.Y ? 0 : dy;
        int targetZ = pushAxis == Direction.Axis.Z ? 0 : dz;
        if (targetX == 0 && targetY == 0 && targetZ == 0) return false;
        int perpDist = Math.max(Math.abs(targetX), Math.max(Math.abs(targetY), Math.abs(targetZ)));
        return perpDist <= PistonStructureResolver.MAX_PUSH_DEPTH;
    }
}
