package net.mehvahdjukaar.supplementaries.common.utils;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin marker added to {@link net.minecraft.world.level.block.piston.PistonMovingBlockEntity}.
 * Lets a pulley smuggle the source block's BlockEntity data through the animation so
 * chests/barrels/etc. survive the move. Applied in the mixin's {@code finalTick} hook
 * after vanilla places the final block state.
 */
public interface ICarryingMovingPiston {
    void supp$setCarriedBlockEntityNbt(@Nullable CompoundTag nbt);

    @Nullable
    CompoundTag supp$getCarriedBlockEntityNbt();
}
