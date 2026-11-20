package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.supplementaries.common.block.cauldron.MovedFluidFiller;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.BlockMovementHelper;
import net.mehvahdjukaar.supplementaries.common.misc.block_movement.ICarryingMovingPiston;
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
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    @WrapOperation(method = "isPushable",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"))
    private static boolean supp$allowBlockEntityPush(BlockState state, Operation<Boolean> original) {
        if (!BlockMovementHelper.isBEMovementIsHandledByUs()) return original.call(state);
        if (BlockMovementHelper.isMovementBlacklisted(state)) return original.call(state);
        return false;
    }

    @WrapOperation(method = "moveBlocks",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private void supp$captureBeForPistonMove(Level level, BlockEntity movingPiston,
                                             Operation<Void> original,
                                             @Local(argsOnly = true) Direction facing,
                                             @Local(argsOnly = true) boolean extending,
                                             @Share("suppDest") LocalRef<BlockPos> destShare) {
        BlockEntity target = movingPiston;
        if (target == null) {
            // Carpet (and possibly others) redirects newMovingBlockEntity to return null, places the real moving
            // piston BE itself right before this call, and turns this call into a no-op. Find the BE it placed.
            BlockPos destPos = destShare.get();
            BlockEntity placed = destPos == null ? null : level.getBlockEntity(destPos);
            if (placed instanceof ICarryingMovingPiston) target = placed;
        }
        if (target == null || !BlockMovementHelper.isBEMovementIsHandledByUs()) {
            original.call(level, movingPiston);
            return;
        }
        Direction direction = extending ? facing : facing.getOpposite();
        BlockPos sourcePos = target.getBlockPos().relative(direction.getOpposite());
        CompoundTag compound = BlockMovementHelper.captureAndDetachBlockEntity(level, sourcePos);
        if (compound != null && target instanceof ICarryingMovingPiston carrying) {
            carrying.supp$setCarriedBlockEntityNbt(compound);
        }
        original.call(level, movingPiston);
    }

    @Inject(method = "moveBlocks",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;getToPush()Ljava/util/List;"))
    private void supp$snapshotFluids(Level level, BlockPos pos, Direction facing, boolean extending,
                                     CallbackInfoReturnable<Boolean> cir,
                                     @Local PistonStructureResolver resolver,
                                     @Share("suppFluids") LocalRef<Map<BlockPos, FluidState>> fluidShare) {
        fluidShare.set(MovedFluidFiller.snapshotFluidsLeftBehind(level, resolver.getToPush(), resolver.getToDestroy()));
    }

    @WrapOperation(method = "moveBlocks",
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lnet/minecraft/world/level/block/piston/MovingPistonBlock;newMovingBlockEntity(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;ZZ)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private BlockEntity supp$moveIntoFluid(BlockPos destPos, BlockState movingPistonState, BlockState movedState,
                                           Direction facing, boolean extending, boolean isSourcePiston,
                                           Operation<BlockEntity> original,
                                           @Local(argsOnly = true) Level level,
                                           @Share("suppFluids") LocalRef<Map<BlockPos, FluidState>> fluidShare,
                                           @Share("suppDest") LocalRef<BlockPos> destShare) {
        destShare.set(destPos);
        FluidState fluid = MovedFluidFiller.getSnapshotFluid(fluidShare.get(), destPos);
        movedState = MovedFluidFiller.getStateMovedInto(movedState, level, destPos, fluid);
        BlockEntity movingBe = original.call(destPos, movingPistonState, movedState, facing, extending, isSourcePiston);
        MovedFluidFiller.markForPostPlacement(movingBe, movedState, fluid);
        return movingBe;
    }

    //slice needed since forge has a different body ad diff calls
    @WrapOperation(method = "moveBlocks",
            slice = @Slice(from = @At(value = "INVOKE", target = "Ljava/util/Map;keySet()Ljava/util/Set;")),
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean supp$leaveFluidBehind(Level level, BlockPos vacatedPos, BlockState air, int flags,
                                          Operation<Boolean> original,
                                          @Share("suppFluids") LocalRef<Map<BlockPos, FluidState>> fluidShare) {

        BlockState left = MovedFluidFiller.getSnapshotFluid(fluidShare.get(), vacatedPos).createLegacyBlock();

        return original.call(level, vacatedPos, left, flags);
    }
}
