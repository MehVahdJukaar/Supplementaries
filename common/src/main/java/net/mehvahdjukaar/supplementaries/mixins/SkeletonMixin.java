package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Skeleton.class)
public abstract class SkeletonMixin extends AbstractSkeleton implements IQuiverEntity {
    //frick it going full mixin here. I could have used caps and spawn events...

    //server
    @Unique
    private ItemStack supplementaries$quiver = ItemStack.EMPTY;
    @Unique
    private float supplementaries$quiverDropChance = 0.6f;

    //for just used to sync this to client
    @Unique
    private static final EntityDataAccessor<Boolean> HAS_QUIVER =
            SynchedEntityData.defineId(Skeleton.class, EntityDataSerializers.BOOLEAN);

    protected SkeletonMixin(EntityType<? extends AbstractSkeleton> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    protected void defineSynchedData(CallbackInfo ci) {
        this.getEntityData().define(HAS_QUIVER, false);
    }

    @Inject(method = "dropCustomDeathLoot", at = @At("TAIL"))
    protected void dropCustomDeathLoot(DamageSource damageSource, int looting, boolean hitByPlayer, CallbackInfo ci) {
        if (this.supplementaries$quiver != null && hitByPlayer) {
            ItemStack itemStack = this.supplementaries$quiver;
            if (Math.max(this.random.nextFloat() - looting * 0.02F, 0.0F) < supplementaries$quiverDropChance) {
                this.spawnAtLocation(itemStack);
                this.supplementaries$quiver = ItemStack.EMPTY;
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (!this.supplementaries$quiver.isEmpty()) {
            compound.put("Quiver", supplementaries$quiver.save(new CompoundTag()));
            compound.putFloat("QuiverDropChance", supplementaries$quiverDropChance);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("Quiver")) {
            this.supplementaries$setQuiver(ItemStack.of(compound.getCompound("Quiver")));
            this.supplementaries$quiverDropChance = compound.getFloat("QuiverDropChance");
        }
    }

    @Override
    public ItemStack supplementaries$getQuiver() {
        return supplementaries$quiver;
    }

    @Override
    public boolean supplementaries$hasQuiver() {
        if (this.level() != null && this.level().isClientSide) {
            return this.getEntityData().get(HAS_QUIVER);
        }
        return IQuiverEntity.super.supplementaries$hasQuiver();
    }

    @Override
    public void supplementaries$setQuiver(ItemStack quiver) {
        this.supplementaries$quiver = quiver;
        this.getEntityData().set(HAS_QUIVER, !quiver.isEmpty());
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        if (this.supplementaries$quiver == null && stack.getItem() == ModRegistry.QUIVER_ITEM.get()) return true;
        return super.wantsToPickUp(stack);
    }

    @Override
    public ItemStack equipItemIfPossible(ItemStack stack) {
        if (stack.getItem() == ModRegistry.QUIVER_ITEM.get()) {
            if (this.supplementaries$quiver != null) {
                this.spawnAtLocation(supplementaries$quiver);
            }
            this.supplementaries$setQuiver(stack);
            this.supplementaries$quiverDropChance = 1;
            return stack;
        }
        return super.equipItemIfPossible(stack);
    }
}