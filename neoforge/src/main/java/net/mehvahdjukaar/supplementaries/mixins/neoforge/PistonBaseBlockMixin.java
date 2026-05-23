package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.misc.PistonCooperationTracker;
import net.mehvahdjukaar.supplementaries.common.utils.ICooperativePiston;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;
import java.util.Set;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    /**
     * Intercepts the feasibility resolve in checkIfExtend to enable cooperative pushing.
     * <p>
     * Protocol per piston:
     * 1. Register this piston as attempting to extend this tick.
     * 2. Try the vanilla single-piston resolve. If it succeeds, mark this piston as
     * posted and let vanilla post the block event normally.
     * 3. If vanilla fails, look for adjacent same-direction pistons that registered
     * earlier this tick. If none exist, return false as usual.
     * 4. Build a cooperative resolver over all pistons in the group and run it. If it
     * fails, return false.
     * 5. Manually post block events for any cooperators whose events weren't posted
     * yet (i.e. those that ran before us and also failed the vanilla resolve). Mark
     * all as posted, then return true so vanilla posts this piston's own event.
     */
    @WrapOperation(method = "checkIfExtend",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z"))
    private boolean supp$wrapCheckIfExtendResolve(PistonStructureResolver resolver, Operation<Boolean> original,
                                                  @Local Level level,
                                                  @Local(ordinal = 0) BlockPos pos,
                                                  @Local Direction direction) {
        long tick = level.getGameTime();
        PistonCooperationTracker.markAttempting(pos, direction, tick);

        boolean vanillaResult = original.call(resolver);
        if (vanillaResult) {
            PistonCooperationTracker.markPosted(pos, tick);
            return true;
        }

        Set<BlockPos> cooperators = PistonCooperationTracker.getCooperators(pos, direction, tick);
        if (cooperators.isEmpty()) {
            return false;
        }

        // Build the full cooperative piston group and a fresh resolver for it.
        Set<BlockPos> allPistons = new HashSet<>(cooperators);
        allPistons.add(pos);
        int limit = allPistons.size() * 12;

        PistonStructureResolver coopResolver = new PistonStructureResolver(level, pos, direction, true);
        ((ICooperativePiston) coopResolver).supp$setCooperators(allPistons, limit);

        if (!coopResolver.resolve()) {
            return false;
        }

        // Post block events for cooperators that failed their own vanilla resolve (not yet posted).
        for (BlockPos cooperatorPos : cooperators) {
            if (!PistonCooperationTracker.hasPosted(cooperatorPos, tick)) {
                level.blockEvent(cooperatorPos,
                        level.getBlockState(cooperatorPos).getBlock(),
                        0,
                        direction.get3DDataValue());
                PistonCooperationTracker.markPosted(cooperatorPos, tick);
            }
        }
        PistonCooperationTracker.markPosted(pos, tick);
        return true; // vanilla will post the block event for this piston
    }

    /**
     * Injects cooperative data into the movement resolver in moveBlocks so that the
     * actual block movement honours the combined push limit of the piston group.
     * <p>
     * Block events fire in the same game tick as checkIfExtend, so the tracker still
     * holds the cooperation group registered during checkIfExtend.
     */
    @WrapOperation(method = "moveBlocks",
            at = @At(value = "NEW",
                    target = "net/minecraft/world/level/block/piston/PistonStructureResolver"))
    private PistonStructureResolver supp$wrapMoveBlocksResolver(Level level, BlockPos pos, Direction facing,
                                                                boolean extending,
                                                                Operation<PistonStructureResolver> original) {
        PistonStructureResolver resolver = original.call(level, pos, facing, extending);

        Set<BlockPos> cooperators = PistonCooperationTracker.getCooperators(pos, facing, level.getGameTime());
        if (!cooperators.isEmpty()) {
            Set<BlockPos> allPistons = new HashSet<>(cooperators);
            allPistons.add(pos);
            int limit = allPistons.size() * 12;
            ((ICooperativePiston) resolver).supp$setCooperators(allPistons, limit);
        }
        return resolver;
    }
}
