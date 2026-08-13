package net.mehvahdjukaar.supplementaries.common.misc.block_movement;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public interface ICarryingMovingPiston {

    void supp$setCarriedBlockEntityNbt(@Nullable CompoundTag nbt);

    @Nullable
    CompoundTag supp$getCarriedBlockEntityNbt();

    //block entity built from the carried nbt, cached so we don't parse it every frame
    @Nullable
    BlockEntity supp$getOrCreateCachedCarriedBlockEntity();

    //call right after the moved block is placed, from both tick and finalTick
    void supp$restoreCarriedBe();

    //fluid that was at the destination, so cauldrons and such can be filled once they land
    void supp$setMovedFluidFill(@Nullable FluidState fluid);

    void supp$applyMovedFluidFill();
}
