package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.utils.ICarryingMovingPiston;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
     * BE data is preserved by {@link #supp$captureBeForPistonMove}.
     */
    @WrapOperation(method = "isPushable",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;hasBlockEntity()Z"))
    private static boolean supp$allowBlockEntityPush(BlockState state, Operation<Boolean> original) {
        if (!CommonConfigs.Tweaks.PUSH_BLOCK_ENTITIES.get()) return original.call(state);
        return false;
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
        if (!CommonConfigs.Tweaks.PUSH_BLOCK_ENTITIES.get()) {
            original.call(level, movingPiston);
            return;
        }
        Direction direction = extending ? facing : facing.getOpposite();
        BlockPos sourcePos = movingPiston.getBlockPos().relative(direction.getOpposite());
        BlockEntity sourceBE = level.getBlockEntity(sourcePos);
        CompoundTag nbt = sourceBE != null ? sourceBE.saveWithFullMetadata(level.registryAccess()) : null;
        // Detach the source BE BEFORE vanilla's source-to-AIR loop runs (and before the
        // next push iteration overwrites this position with MOVING_PISTON). Container
        // blocks' onRemove fetches the BE to drop contents — a null BE skips the drop.
        // Lectern's popBook also fetches the BE for the same reason.
        if (sourceBE != null) {
            level.removeBlockEntity(sourcePos);
        }
        // Attach the carried NBT to the moving piston BE BEFORE it goes into the chunk,
        // so any first query (renderer, ticker) immediately sees the carried data.
        if (nbt != null && movingPiston instanceof ICarryingMovingPiston carrying) {
            carrying.supp$setCarriedBlockEntityNbt(nbt);
        }
        original.call(level, movingPiston);
    }
}
