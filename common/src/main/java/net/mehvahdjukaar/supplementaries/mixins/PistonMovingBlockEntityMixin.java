package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.cauldron.MovedFluidFiller;
import net.mehvahdjukaar.supplementaries.common.misc.cooperative.ICarryingMovingPiston;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonMovingBlockEntityMixin extends BlockEntity implements ICarryingMovingPiston {

    @Unique
    @Nullable
    private CompoundTag supp$carriedBeNbt;

    @Unique
    @Nullable
    private BlockEntity supp$cachedCarriedBE;

    @Unique
    @Nullable
    private FluidState supp$movedFluidFill;

    public PistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void supp$setMovedFluidFill(@Nullable FluidState fluid) {
        this.supp$movedFluidFill = fluid;
    }

    @Override
    public void supp$applyMovedFluidFill() {
        FluidState fluid = this.supp$movedFluidFill;
        this.supp$movedFluidFill = null;
        if (fluid == null || this.level == null) return;
        MovedFluidFiller.applyPostPlacement(this.level, this.worldPosition, fluid);
    }

    @Override
    public void supp$setCarriedBlockEntityNbt(@Nullable CompoundTag nbt) {
        this.supp$carriedBeNbt = nbt;
        this.supp$cachedCarriedBE = null;
    }

    @Override
    @Nullable
    public CompoundTag supp$getCarriedBlockEntityNbt() {
        return this.supp$carriedBeNbt;
    }

    @Override
    @Nullable
    public BlockEntity supp$getOrCreateCachedCarriedBlockEntity() {
        if (this.supp$cachedCarriedBE != null) return this.supp$cachedCarriedBE;
        if (this.level == null) return null;
        PistonMovingBlockEntity self = (PistonMovingBlockEntity) (Object) this;
        BlockState movedState = self.getMovedState();
        if (!(movedState.getBlock() instanceof EntityBlock entityBlock)) return null;
        BlockEntity be = entityBlock.newBlockEntity(this.worldPosition, movedState);
        if (be == null) return null;
        // Render with default state if NBT hasn't arrived yet — covers the brief gap when
        // the moving piston BE is constructed locally on the client (block event handler)
        // before our hook attaches the carried NBT. When NBT arrives, the setter
        // invalidates the cache and the next call rebuilds with the NBT applied.
        CompoundTag nbt = this.supp$carriedBeNbt;
        if (nbt != null
                && be.getType() == BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceLocation.parse(nbt.getString("id")))) {
            be.loadWithComponents(nbt, this.level.registryAccess());
        }
        be.setLevel(this.level);
        be.clearRemoved();
        this.supp$cachedCarriedBE = be;
        return be;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void supp$saveCarriedBeNbt(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (this.supp$carriedBeNbt != null) {
            tag.put("supp_carried_be", this.supp$carriedBeNbt.copy());
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void supp$loadCarriedBeNbt(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (tag.contains("supp_carried_be", CompoundTag.TAG_COMPOUND)) {
            this.supp$carriedBeNbt = tag.getCompound("supp_carried_be").copy();
        }
    }

    // Deliberately not gated on blockEntityMovesHandledByUs: pulley moves reach this through the
    // inherited finalTick and must keep working even when Quark owns piston moves. For a
    // Quark-owned piston move our capture never ran, so there is no NBT here and this no-ops
    // rather than replacing the block entity Quark just populated.
    @Override
    public void supp$restoreCarriedBe() {
        CompoundTag nbt = this.supp$carriedBeNbt;
        this.supp$carriedBeNbt = null;
        this.supp$cachedCarriedBE = null;
        if (nbt == null || this.level == null) return;
        BlockState placed = this.level.getBlockState(this.worldPosition);
        if (placed.isAir() || placed.is(Blocks.MOVING_PISTON) || !placed.hasBlockEntity()) return;
        if (!(placed.getBlock() instanceof EntityBlock entityBlock)) return;
        BlockEntity restored = entityBlock.newBlockEntity(this.worldPosition, placed);
        if (restored == null) return;
        restored.loadWithComponents(nbt, this.level.registryAccess());
        this.level.setBlockEntity(restored);
    }

    // Normal completion path: tick() places the moved block itself — it does NOT call
    // finalTick. Restore right after vanilla sets the block, covering both the air
    // (updateOrDestroy) and non-air (neighborChanged) branches.
    @Inject(method = "tick", at = {
            @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/level/block/Block;updateOrDestroy(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;I)V"),
            @At(value = "INVOKE", shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/world/level/Level;neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;)V")
    })
    private static void supp$restoreCarriedBeOnTick(Level level, BlockPos pos, BlockState state,
                                                    PistonMovingBlockEntity blockEntity, CallbackInfo ci) {
        ICarryingMovingPiston carrying = (ICarryingMovingPiston) blockEntity;
        carrying.supp$restoreCarriedBe();
        carrying.supp$applyMovedFluidFill();
    }

    // Interrupt path: a new piston action force-finishes this move before the animation
    // completes, calling finalTick() instead of letting tick() finish it.
    @Inject(method = "finalTick", at = @At("TAIL"))
    private void supp$restoreCarriedBeOnFinalTick(CallbackInfo ci) {
        this.supp$restoreCarriedBe();
        this.supp$applyMovedFluidFill();
    }
}
