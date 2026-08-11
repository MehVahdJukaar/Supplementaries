package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public interface ICarryingMovingPiston {

    void supp$setCarriedBlockEntityNbt(@Nullable CompoundTag nbt);

    @Nullable
    CompoundTag supp$getCarriedBlockEntityNbt();

    // Transient BlockEntity built from the carried NBT for rendering, cached so it isn't re-parsed
    // every frame. Null when there's nothing carried, the moved state has no BE, or the type differs.
    @Nullable
    BlockEntity supp$getOrCreateCachedCarriedBlockEntity();

    // Applies the carried NBT onto the freshly placed block and clears it. Call right after vanilla
    // places the moved block, from both tick() (normal finish) and finalTick() (forced early).
    void supp$restoreCarriedBe();

    // Smuggles the fluid that occupied the destination so a cauldron (or other IFluidFillableOnMove
    // block) can have its block entity filled once it lands. Only set when post-placement is needed,
    // and not persisted: an interrupted move just leaves the block empty, as it did before.
    void supp$setMovedFluidFill(@Nullable FluidState fluid);

    // Applies the carried fluid fill onto the freshly placed block and clears it. Runs right after
    // supp$restoreCarriedBe on both completion paths.
    void supp$applyMovedFluidFill();
}
