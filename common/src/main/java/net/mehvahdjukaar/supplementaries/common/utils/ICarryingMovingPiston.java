package net.mehvahdjukaar.supplementaries.common.utils;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin marker for {@link net.minecraft.world.level.block.piston.PistonMovingBlockEntity}
 * that lets a pulley smuggle the source block's {@link net.minecraft.world.level.block.entity.BlockEntity}
 * NBT through the moving-piston animation. The mixin's {@code finalTick} hook reads the
 * stored NBT and applies it to the freshly-placed block at the destination, restoring
 * containers/inventories/etc. that vanilla piston movement would silently drop.
 * <p>
 * NBT is captured at the source position immediately before the source's BE is removed,
 * and applied at the destination immediately after {@code movedState} is set.
 */
public interface ICarryingMovingPiston {
    void supp$setCarriedBlockEntityNbt(@Nullable CompoundTag nbt);

    @Nullable
    CompoundTag supp$getCarriedBlockEntityNbt();
}
