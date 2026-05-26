package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.utils.ICarryingMovingPiston;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets a pulley smuggle a source block's BlockEntity NBT through the moving-piston
 * animation. Set via {@link ICarryingMovingPiston#supp$setCarriedBlockEntityNbt} at the
 * destination immediately after the moving-piston BE is created, applied here in
 * {@code finalTick} once vanilla has placed the final block state.
 * <p>
 * Save/load: the NBT is persisted on the moving-piston BE so a mid-animation chunk
 * unload/save doesn't drop the inventory. The "supp_carried_be" tag is namespaced to
 * avoid collision with future vanilla/NeoForge additions to PistonMovingBlockEntity's
 * own NBT.
 */
@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonMovingBlockEntityMixin extends BlockEntity implements ICarryingMovingPiston {

    @Unique
    @Nullable
    private CompoundTag supp$carriedBeNbt;

    public PistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void supp$setCarriedBlockEntityNbt(@Nullable CompoundTag nbt) {
        this.supp$carriedBeNbt = nbt;
    }

    @Override
    @Nullable
    public CompoundTag supp$getCarriedBlockEntityNbt() {
        return this.supp$carriedBeNbt;
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

    /**
     * After vanilla places the final block, attach the carried BlockEntity (rebuilt from
     * the saved NBT) at this position. Skip when there's no BE to host (AIR or non-BE
     * block) — those cases shouldn't happen given our capture conditions, but bail safely.
     */
    @Inject(method = "finalTick", at = @At("TAIL"))
    private void supp$restoreCarriedBe(CallbackInfo ci) {
        if (this.level != null) {
            Supplementaries.LOGGER.info("[PistonMovingBE {} @ {}] finalTick fired (hasCarried={})",
                    this.level.isClientSide ? "client" : "server",
                    this.worldPosition, this.supp$carriedBeNbt != null);
        }
        CompoundTag nbt = this.supp$carriedBeNbt;
        this.supp$carriedBeNbt = null;
        if (nbt == null || this.level == null) return;
        BlockState placed = this.level.getBlockState(this.worldPosition);
        if (placed.isAir() || placed.is(Blocks.MOVING_PISTON) || !placed.hasBlockEntity()) return;
        if (!(placed.getBlock() instanceof EntityBlock entityBlock)) return;
        BlockEntity restored = entityBlock.newBlockEntity(this.worldPosition, placed);
        if (restored == null) return;
        restored.loadWithComponents(nbt, this.level.registryAccess());
        this.level.setBlockEntity(restored);
    }
}
