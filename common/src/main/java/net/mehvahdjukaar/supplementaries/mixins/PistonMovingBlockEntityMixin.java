package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.utils.ICarryingMovingPiston;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonMovingBlockEntityMixin extends BlockEntity implements ICarryingMovingPiston {

    @Unique
    @Nullable
    private CompoundTag supp$carriedBeNbt;

    @Unique
    @Nullable
    private BlockEntity supp$cachedCarriedBE;

    public PistonMovingBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
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

    @Inject(method = "finalTick", at = @At("TAIL"))
    private void supp$restoreCarriedBe(CallbackInfo ci) {
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
