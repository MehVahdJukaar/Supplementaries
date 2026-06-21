package net.mehvahdjukaar.supplementaries.common.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin marker added to {@link net.minecraft.world.level.block.piston.PistonMovingBlockEntity}.
 * Lets a pulley smuggle the source block's BlockEntity data through the animation so
 * chests/barrels/etc. survive the move. Applied via {@link #supp$restoreCarriedBe()} right
 * after vanilla places the final block state (on both the {@code tick} and {@code finalTick}
 * completion paths).
 */
public interface ICarryingMovingPiston {
    void supp$setCarriedBlockEntityNbt(@Nullable CompoundTag nbt);

    @Nullable
    CompoundTag supp$getCarriedBlockEntityNbt();

    /**
     * Lazily builds and caches a transient BlockEntity from the carried NBT for client-side
     * rendering. Avoids re-parsing the NBT every frame. Returns null if there's nothing to
     * carry, the moved state has no BE, or the NBT type doesn't match.
     */
    @Nullable
    BlockEntity supp$getOrCreateCachedCarriedBlockEntity();

    /**
     * Applies the carried NBT onto the freshly placed block (and clears it). Must be called
     * right after vanilla places the moved block. The normal animation finishes in
     * {@code tick()}; {@code finalTick()} only runs when a move is force-completed early, so
     * both paths must invoke this.
     */
    void supp$restoreCarriedBe();
}
