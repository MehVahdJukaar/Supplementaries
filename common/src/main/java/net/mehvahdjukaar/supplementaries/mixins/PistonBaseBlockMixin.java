package net.mehvahdjukaar.supplementaries.mixins;

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

    // isPushable ends on !state.hasBlockEntity(), rejecting every BE block whatever its PushReaction
    // (BLOCK/DESTROY/PUSH_ONLY already returned earlier). Lying here lets those through; the BE data
    // is carried by supp$captureBeForPistonMove. Passing the vanilla value back leaves the block
    // unpushable, which is what we do when Quark owns the move (its own hook on this call decides)
    // and for blacklisted blocks.
    // The blacklist check belongs here, not on the return value: without a block entity vanilla
    // pushes the block on its own, and c:relocation_not_supported is aimed at movers that ignore
    // getPistonPushReaction, not at overriding a block's own push reaction.
    @WrapOperation(method = "isPushable",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"))
    private static boolean supp$allowBlockEntityPush(BlockState state, Operation<Boolean> original) {
        if (!PistonMovementHelper.BEMovementHandledByUs()) return original.call(state);
        if (PistonMovementHelper.isMovementBlacklisted(state)) return original.call(state);
        return false;
    }

    // Hands the source block's BE nbt to the moving-piston BE before it goes into the chunk.
    // Here movingPiston.getBlockPos() is already the target, so the source is one step back along
    // the push direction and still holds its live block entity. PistonMovingBlockEntityMixin saves
    // the nbt and its finalTick applies it when the animation lands.
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

    // A cauldron pushed into a water or lava source lands on a to-destroy position, so keep those
    // fluids around for supp$fillPushedCauldron. Taken at the getToPush call: the structure has
    // resolved, the resolver local is live and the destroy loop hasn't cleared anything yet.
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

    // Swap the moved state for its filled variant when the destination held a fluid, so the
    // animation lands a filled cauldron. Ordinal 0 is the push loop, not the piston-head call.
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
        // blocks) can't be filled by state alone, so carry the fluid and apply it once the moving
        // animation lands the block.
        if (fluid != null && movingBe instanceof ICarryingMovingPiston carrying
                && MovedFluidFiller.needsPostPlacement(movedState)) {
            carrying.supp$setMovedFluidFill(fluid);
        }
        return movingBe;
    }
}
