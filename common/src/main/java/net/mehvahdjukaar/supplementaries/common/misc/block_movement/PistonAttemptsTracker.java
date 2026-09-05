package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class PistonAttemptsTracker {

    private static final Map<ServerLevel, Map<BlockPos, Attempt>> SERVER_ATTEMPTS = new WeakHashMap<>();
    private static final Map<BlockPos, ClientCooperation> CLIENT_PENDING = new HashMap<>();

    private record Attempt(Direction direction, boolean extending, long tick) {
    }

    private record ClientCooperation(Set<BlockPos> cooperators, long receivedTick) {
    }

    public static void mark(ServerLevel level, BlockPos pos, Direction direction, boolean extending) {
        long now = level.getGameTime();
        Map<BlockPos, Attempt> attempts = SERVER_ATTEMPTS.computeIfAbsent(level, l -> new HashMap<>());
        attempts.values().removeIf(a -> !isFresh(a, now));
        attempts.put(pos.immutable(), new Attempt(direction, extending, now));
    }

    public static Set<BlockPos> getCooperators(ServerLevel level, BlockPos pistonPos, Direction direction, boolean extending) {
        Set<BlockPos> cooperators = new HashSet<>();
        Map<BlockPos, Attempt> attempts = SERVER_ATTEMPTS.get(level);
        if (attempts == null) return cooperators;
        long now = level.getGameTime();
        for (Map.Entry<BlockPos, Attempt> entry : attempts.entrySet()) {
            Attempt attempt = entry.getValue();
            BlockPos candidate = entry.getKey();
            if (!isFresh(attempt, now) || attempt.direction() != direction || attempt.extending() != extending)
                continue;
            if (!canReachSameStructure(candidate, pistonPos, direction.getAxis())) continue;
            if (!isStillAboutToMove(level, candidate, direction, extending)) continue;
            cooperators.add(candidate);
        }
        return cooperators;
    }

    public static void onClientCooperatorsReceived(Level level, BlockPos pos, Set<BlockPos> cooperators) {
        long now = level.getGameTime();
        CLIENT_PENDING.values().removeIf(c -> now - c.receivedTick() > 1);
        CLIENT_PENDING.put(pos, new ClientCooperation(cooperators, now));
    }

    @Nullable
    public static Set<BlockPos> takeClientCooperators(Level level, BlockPos pos) {
        ClientCooperation pending = CLIENT_PENDING.remove(pos);
        if (pending == null || level.getGameTime() - pending.receivedTick() > 1) return null;
        return pending.cooperators();
    }

    //block events run at the end of the tick, or the next one when the update came from a packet
    private static boolean isFresh(Attempt attempt, long now) {
        return now - attempt.tick() <= 1;
    }

    private static boolean isStillAboutToMove(ServerLevel level, BlockPos pos, Direction direction, boolean extending) {
        if (!level.isLoaded(pos)) return false;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof PistonBaseBlock piston)) return false;
        if (state.getValue(PistonBaseBlock.FACING) != direction) return false;
        if (state.getValue(PistonBaseBlock.EXTENDED) == extending) return false;
        if (!extending && (!piston.isSticky || !level.getBlockState(pos.relative(direction)).is(Blocks.PISTON_HEAD)))
            return false;
        return piston.getNeighborSignal(level, pos, direction) == extending;
    }

    //MAX_PUSH_DEPTH and not the configured limit, this is how far a structure can reach. same push line is out
    private static boolean canReachSameStructure(BlockPos candidate, BlockPos pistonPos, Direction.Axis pushAxis) {
        int dx = candidate.getX() - pistonPos.getX();
        int dy = candidate.getY() - pistonPos.getY();
        int dz = candidate.getZ() - pistonPos.getZ();
        int along = Math.abs(pushAxis.choose(dx, dy, dz));
        if (along >= PistonStructureResolver.MAX_PUSH_DEPTH) return false;

        int acrossX = pushAxis == Direction.Axis.X ? 0 : Math.abs(dx);
        int acrossY = pushAxis == Direction.Axis.Y ? 0 : Math.abs(dy);
        int acrossZ = pushAxis == Direction.Axis.Z ? 0 : Math.abs(dz);
        int across = Math.max(acrossX, Math.max(acrossY, acrossZ));
        return across > 0 && across <= PistonStructureResolver.MAX_PUSH_DEPTH;
    }
}
