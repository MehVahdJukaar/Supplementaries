package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
 import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.CooperativePistonData;
import net.mehvahdjukaar.supplementaries.common.utils.ICooperativePiston;
import net.mehvahdjukaar.supplementaries.reg.ModData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    /**
     * Intercepts the extension feasibility resolve in checkIfExtend.
     * <p>
     * Protocol per piston:
     *  1. Register this piston as attempting to extend this tick.
     *  2. Try the vanilla single-piston resolve. If it succeeds, mark posted and let
     *     vanilla post the block event normally.
     *  3. If vanilla fails, look for same-direction pistons that registered earlier this
     *     tick. If none exist, return false as usual.
     *  4. Build a cooperative resolver over all pistons in the group and run it. If it
     *     fails, return false.
     *  5. Manually post block events for any cooperators whose events weren't posted yet,
     *     then return true so vanilla posts this piston's event.
     */
    @WrapOperation(method = "checkIfExtend",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;resolve()Z"))
    private boolean supp$wrapCheckIfExtendResolve(PistonStructureResolver resolver, Operation<Boolean> original,
                                                  @Local(argsOnly = true) Level level,
                                                  @Local(argsOnly = true) BlockPos pos,
                                                  @Local Direction direction) {
        if (!(level instanceof ServerLevel serverLevel)) return original.call(resolver);

        long tick = level.getGameTime();
        CooperativePistonData data = ModData.COOPERATIVE_PISTONS.getData(serverLevel);
        data.markAttempting(pos, direction, true, tick);
        CooperativePistonData.markAttemptingClient(pos, direction, true, tick);
        Supplementaries.LOGGER.info("[COOP] checkIfExtend@{} dir={} tick={} (marked attempting)", pos, direction, tick);

        boolean vanillaResult = original.call(resolver);
        Supplementaries.LOGGER.info("[COOP] checkIfExtend@{} vanilla resolve={}", pos, vanillaResult);
        if (vanillaResult) {
            data.markPosted(pos, tick);
            return true;
        }

        Set<BlockPos> cooperators = data.getCooperators(pos, direction, true, tick);
        Supplementaries.LOGGER.info("[COOP] checkIfExtend@{} candidate cooperators={}", pos, cooperators);
        if (cooperators.isEmpty()) {
            return false;
        }

        Set<BlockPos> allPistons = new HashSet<>(cooperators);
        allPistons.add(pos);
        int limit = allPistons.size() * 12;

        PistonStructureResolver coopResolver = new PistonStructureResolver(level, pos, direction, true);
        ((ICooperativePiston) coopResolver).supp$setCooperators(allPistons, limit);

        boolean coopResult = coopResolver.resolve();
        Supplementaries.LOGGER.info("[COOP] checkIfExtend@{} coopResolve={}", pos, coopResult);
        if (!coopResult) {
            return false;
        }

        // Only post events for cooperators that actually shared the pushed structure.
        // Free-riders (air above, disjoint columns) are filtered out by the resolver's
        // contribution check; their vanilla resolve already succeeded independently if
        // applicable, so no event is owed here.
        Set<BlockPos> contributing = ((ICooperativePiston) coopResolver).supp$getContributingCooperators();
        Supplementaries.LOGGER.info("[COOP] checkIfExtend@{} contributing cooperators={}", pos, contributing);
        for (BlockPos cooperatorPos : contributing) {
            if (!data.hasPosted(cooperatorPos, tick)) {
                level.blockEvent(cooperatorPos,
                        level.getBlockState(cooperatorPos).getBlock(),
                        0,
                        direction.get3DDataValue());
                data.markPosted(cooperatorPos, tick);
            }
        }
        data.markPosted(pos, tick);
        return true;
    }

    /**
     * Register sticky pistons that are about to retract. Retraction's checkIfExtend never
     * creates a resolver — it just posts a block event with id 1 or 2 — so we hook the
     * blockEvent call itself to register the piston for cooperative pulling.
     * <p>
     * ordinal = 1 targets the second {@code Level.blockEvent(BlockPos, Block, int, int)}
     * call in checkIfExtend (the first is the extension event in the !extended branch).
     */
    @Inject(method = "checkIfExtend",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;blockEvent(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;II)V",
                    ordinal = 1))
    private void supp$registerRetraction(Level level, BlockPos pos, BlockState state, CallbackInfo ci,
                                         @Local Direction direction) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        long tick = level.getGameTime();
        ModData.COOPERATIVE_PISTONS.getData(serverLevel).markAttempting(pos, direction, false, tick);
        CooperativePistonData.markAttemptingClient(pos, direction, false, tick);
    }

    /**
     * Injects cooperative data into the movement resolver in moveBlocks. Block events fire
     * in the tick following checkIfExtend, so the tracker still holds the cooperation group
     * registered during checkIfExtend (entries are filtered by age and extending state).
     * <p>
     * For retraction, we also pre-retract each cooperator: set its body to MOVING_PISTON
     * (mimicking what its own triggerEvent would do moments later) and then remove its head.
     * Two reasons for this:
     *  1. With the head still present, the cooperative sticky-branching forward-scan from
     *     one column hits the other column's PISTON_HEAD (push reaction BLOCK) and fails.
     *  2. Setting body → MOVING_PISTON before head → AIR is the ORDER vanilla retraction
     *     uses. If we removed the head while the body is still STICKY_PISTON,
     *     PistonHeadBlock.onRemove sees the still-extended body behind it and
     *     destroyBlock()s it — popping the piston as an item.
     */
    @WrapOperation(method = "moveBlocks",
            at = @At(value = "NEW",
                    target = "net/minecraft/world/level/block/piston/PistonStructureResolver"))
    private PistonStructureResolver supp$wrapMoveBlocksResolver(Level level, BlockPos pos, Direction facing,
                                                                boolean extending,
                                                                Operation<PistonStructureResolver> original) {
        final Set<BlockPos> cooperators;
        if (level instanceof ServerLevel serverLevel) {
            cooperators = ModData.COOPERATIVE_PISTONS.getData(serverLevel)
                    .getCooperators(pos, facing, extending, level.getGameTime());
        } else {
            cooperators = CooperativePistonData.getCooperatorsClient(pos, facing, extending);
        }

        if (!extending && !cooperators.isEmpty()) {
            for (BlockPos cooperatorPos : cooperators) {
                supp$preRetractCooperator(level, cooperatorPos, facing);
            }
        }

        PistonStructureResolver resolver = original.call(level, pos, facing, extending);

        if (!cooperators.isEmpty()) {
            Set<BlockPos> allPistons = new HashSet<>(cooperators);
            allPistons.add(pos);
            int limit = allPistons.size() * 12;
            ((ICooperativePiston) resolver).supp$setCooperators(allPistons, limit);
            Supplementaries.LOGGER.info("[COOP] moveBlocks@{} facing={} extending={} cooperators={}",
                    pos, facing, extending, cooperators);
        }
        return resolver;
    }

    @Unique
    private static void supp$preRetractCooperator(Level level, BlockPos cooperatorPos, Direction facing) {
        BlockState bodyState = level.getBlockState(cooperatorPos);
        if (!(bodyState.getBlock() instanceof PistonBaseBlock)) return;

        BlockPos headPos = cooperatorPos.relative(facing);
        if (!level.getBlockState(headPos).is(Blocks.PISTON_HEAD)) return;

        // 1. Convert the cooperator's body to MOVING_PISTON (matches what its own
        //    triggerEvent would do moments later — that call will be a no-op since the
        //    block state matches; only the block entity gets re-created).
        PistonType type = bodyState.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT;
        BlockState movingPistonState = Blocks.MOVING_PISTON.defaultBlockState()
                .setValue(MovingPistonBlock.FACING, facing)
                .setValue(MovingPistonBlock.TYPE, type);
        BlockState retractedBody = bodyState.setValue(PistonBaseBlock.EXTENDED, false);
        level.setBlock(cooperatorPos, movingPistonState, 20);
        level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(
                cooperatorPos, movingPistonState, retractedBody, facing, false, true));

        // 2. Now safe to remove the head — PistonHeadBlock.onRemove will see the body
        //    is MOVING_PISTON (not STICKY_PISTON) and skip the destroy-and-drop logic.
        level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 20);
    }
}
