package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.supplementaries.common.block.cauldron.MovedFluidFiller;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.PistonMovementHelper;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ICarryingMovingPiston;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    /**
     * Remove the vanilla hard-block on pushing block entities. The last line of
     * isPushable returns {@code !state.hasBlockEntity()}, which rejects every block
     * with a BE regardless of PushReaction. Returning false here makes it appear as
     * though the block has no block entity, so blocks whose PushReaction is NORMAL
     * pass through. Blocks with BLOCK/DESTROY/PUSH_ONLY are already handled earlier
     * in isPushable and never reach this call.
     * <p>
     * BE data is preserved by {@link #supp$captureBeForPistonMove}. When Quark owns the move
     * (see {@link PistonMovementHelper#BEMovementHandledByUs()}) we pass the vanilla value
     * through untouched so its own hook on this same call decides, which also keeps its movement
     * blacklist authoritative.
     */
    @WrapOperation(method = "isPushable",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"))
    private static boolean supp$allowBlockEntityPush(BlockState state, Operation<Boolean> original) {
        if (!PistonMovementHelper.BEMovementHandledByUs()) return original.call(state);
        return false;
    }

    /**
     * Single enforcement point for "this block is never relocated". Pulleys and ropes run their
     * pushability through this same method, so one tag covers every mover we have. See
     * {@link PistonMovementHelper#isMovementBlacklisted}.
     */
    @ModifyReturnValue(method = "isPushable", at = @At("RETURN"))
    private static boolean supp$respectMovementBlacklist(boolean original,
                                                         @Local(argsOnly = true) BlockState state) {
        return original && !PistonMovementHelper.isMovementBlacklisted(state);
    }

    /**
     * Before vanilla places the {@code MOVING_PISTON} block entity at the target
     * position, snapshot the source block's block entity NBT and attach it to the
     * new moving-piston BE via {@link ICarryingMovingPiston}.
     * <p>
     * At this call site {@code movingPiston.getBlockPos()} is already the target
     * position; the source is one step opposite to the push direction. The source
     * block has not been removed yet, so {@code level.getBlockEntity(source)} is
     * still live.
     * <p>
     * The NBT is persisted inside the moving-piston BE (save/load handled by
     * {@link PistonMovingBlockEntityMixin}) and applied by its {@code finalTick}
     * once the animation completes.
     */
    @WrapOperation(method = "moveBlocks",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private void supp$captureBeForPistonMove(Level level, BlockEntity movingPiston,
                                             Operation<Void> original,
                                             @Local(argsOnly = true) Direction facing,
                                             @Local(argsOnly = true) boolean extending) {
        if (movingPiston == null) {
            // Vanilla's newMovingBlockEntity never returns null and Level.setBlockEntity dereferences
            throw new IllegalStateException("Another mod passed a null moving-piston BlockEntity into " +
                    "Level.setBlockEntity during PistonBaseBlock.moveBlocks. This is not a Supplementaries bug; " +
                    "some other mod is overriding MovingPistonBlock.newMovingBlockEntity and returning null. " +
                    "Carpet is a known mod that does this.");
        }
        if (!PistonMovementHelper.BEMovementHandledByUs()) {
            original.call(level, movingPiston);
            return;
        }
        Direction direction = extending ? facing : facing.getOpposite();
        BlockPos sourcePos = movingPiston.getBlockPos().relative(direction.getOpposite());
        // Capture and detach BEFORE vanilla's source-to-AIR loop runs, and before the next push
        // iteration overwrites this position with MOVING_PISTON.
        CompoundTag nbt = PistonMovementHelper.captureAndDetachBlockEntity(level, sourcePos);
        // Attach the carried NBT to the moving piston BE BEFORE it goes into the chunk,
        // so any first query (renderer, ticker) immediately sees the carried data.
        if (nbt != null && movingPiston instanceof ICarryingMovingPiston carrying) {
            carrying.supp$setCarriedBlockEntityNbt(nbt);
        }
        original.call(level, movingPiston);
    }

    /**
     * Snapshot the fluid states of the positions the piston is about to destroy, before the
     * destroy loop clears them to air. A cauldron pushed into a water/lava source lands on one
     * of these positions, so we keep the original fluid around to fill it in
     * {@link #supp$fillPushedCauldron}.
     * <p>
     * Captured at the {@code getToPush} call: the structure has already resolved, the resolver
     * local is live, and the to-destroy fluids are still in the world.
     */
    @Inject(method = "moveBlocks",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;getToPush()Ljava/util/List;"))
    private void supp$snapshotCauldronFluids(Level level, BlockPos pos, Direction facing, boolean extending,
                                             CallbackInfoReturnable<Boolean> cir,
                                             @Local PistonStructureResolver resolver,
                                             @Share("suppCauldronFluids") LocalRef<Map<BlockPos, FluidState>> fluidShare) {
        if (!CommonConfigs.Tweaks.MOVED_CAULDRON_FILLING.get()) return;
        Map<BlockPos, FluidState> fluids = new HashMap<>();
        for (BlockPos destroyed : resolver.getToDestroy()) {
            FluidState fluid = level.getFluidState(destroyed);
            if (!fluid.isEmpty()) fluids.put(destroyed.immutable(), fluid);
        }
        fluidShare.set(fluids);
    }

    /**
     * When a pushed cauldron's destination used to hold a fluid (captured in
     * {@link #supp$snapshotCauldronFluids}), swap the moved state for the filled cauldron so the
     * moving-piston animation lands a filled one. Ordinal 0 targets the push-loop call only, not
     * the piston-head call.
     */
    @WrapOperation(method = "moveBlocks",
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private BlockEntity supp$fillPushedCauldron(BlockPos destPos, BlockState movingPistonState, BlockState movedState,
                                                Direction facing, boolean extending, boolean isSourcePiston,
                                                Operation<BlockEntity> original,
                                                @Local(argsOnly = true) Level level,
                                                @Share("suppCauldronFluids") LocalRef<Map<BlockPos, FluidState>> fluidShare) {
        Map<BlockPos, FluidState> fluids = fluidShare.get();
        FluidState fluid = fluids != null ? fluids.get(destPos) : null;
        if (fluid != null) {
            movedState = MovedFluidFiller.fillIfMovedIntoFluid(movedState, level, destPos, fluid);
        }
        BlockEntity movingBe = original.call(destPos, movingPistonState, movedState, facing, extending, isSourcePiston);
        // Cauldrons that keep their fluid in a block entity (Amendments liquid cauldrons, opted-in
        // blocks) can't be filled by state alone — carry the fluid so it's applied once the moving
        // animation lands the block.
        if (fluid != null && movingBe instanceof ICarryingMovingPiston carrying
                && MovedFluidFiller.needsPostPlacement(movedState)) {
            carrying.supp$setMovedFluidFill(fluid);
        }
        return movingBe;
    }
}
