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

    @WrapOperation(method = "isPushable",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"))
    private static boolean supp$allowBlockEntityPush(BlockState state, Operation<Boolean> original) {
        if (!PistonMovementHelper.isBEMovementIsHandledByUs()) return original.call(state);
        if (PistonMovementHelper.isMovementBlacklisted(state)) return original.call(state);
        return false;
    }

    @WrapOperation(method = "moveBlocks",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
    private void supp$captureBeForPistonMove(Level level, BlockEntity movingPiston,
                                             Operation<Void> original,
                                             @Local(argsOnly = true) Direction facing,
                                             @Local(argsOnly = true) boolean extending) {
        if (movingPiston == null) {
            //nonsense check, idk what mod triggered this. pls report if you know
            throw new IllegalStateException("Another mod passed a null moving-piston BlockEntity into " +
                    "Level.setBlockEntity during PistonBaseBlock.moveBlocks. This is not a Supplementaries bug; " +
                    "some other mod is overriding MovingPistonBlock.newMovingBlockEntity and returning null. " +
                    "Carpet is a known mod that does this.");
        }
        if (!PistonMovementHelper.isBEMovementIsHandledByUs()) {
            original.call(level, movingPiston);
            return;
        }
        Direction direction = extending ? facing : facing.getOpposite();
        BlockPos sourcePos = movingPiston.getBlockPos().relative(direction.getOpposite());
        CompoundTag compound = PistonMovementHelper.captureAndDetachBlockEntity(level, sourcePos);
        if (compound != null && movingPiston instanceof ICarryingMovingPiston carrying) {
            carrying.supp$setCarriedBlockEntityNbt(compound);
        }
        original.call(level, movingPiston);
    }

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
        if (fluid != null && movingBe instanceof ICarryingMovingPiston carrying
                && MovedFluidFiller.needsPostPlacement(movedState)) {
            carrying.supp$setMovedFluidFill(fluid);
        }
        return movingBe;
    }
}
