package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Stray.class)
public abstract class StrayMixin extends AbstractSkeleton implements IQuiverEntity {
    //frick it going full mixin here. I could have used caps and spawn events...

    //server
    @Unique
    private ItemStack quiver = ItemStack.EMPTY;
    @Unique
    private float quiverDropChance = 0.6f;

    //for just used to sync this to client
    private static final EntityDataAccessor<Boolean> HAS_QUIVER =
            SynchedEntityData.defineId(Stray.class, EntityDataSerializers.BOOLEAN);

    protected StrayMixin(EntityType<? extends AbstractSkeleton> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(HAS_QUIVER, false);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int looting, boolean hitByPlayer) {
        super.dropCustomDeathLoot(damageSource, looting, hitByPlayer);
        if (this.quiver != null && hitByPlayer) {
            ItemStack itemStack = this.quiver;
            if (Math.max(this.random.nextFloat() - (float) looting * 0.02F, 0.0F) < quiverDropChance) {
                this.spawnAtLocation(itemStack);
                this.quiver = ItemStack.EMPTY;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (!this.quiver.isEmpty()) {
            compound.put("Quiver", quiver.save(new CompoundTag()));
            compound.putFloat("QuiverDropChance", quiverDropChance);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Quiver")) {
            this.setQuiver(ItemStack.of(compound.getCompound("Quiver")));
            this.quiverDropChance = compound.getFloat("QuiverDropChance");
        }
    }

    @Override
    public ItemStack getQuiver() {
        return quiver;
    }

    @Override
    public boolean hasQuiver() {
        if (this.level != null && this.level.isClientSide) {
            return this.getEntityData().get(HAS_QUIVER);
        }
        return IQuiverEntity.super.hasQuiver();
    }

    @Override
    public void setQuiver(ItemStack quiver) {
        this.quiver = quiver;
        this.getEntityData().set(HAS_QUIVER, !quiver.isEmpty());
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        if (this.quiver == null && stack.getItem() == ModRegistry.QUIVER_ITEM.get()) return true;
        return super.wantsToPickUp(stack);
    }

    @Override
    public boolean equipItemIfPossible(ItemStack stack) {
        if (stack.getItem() == ModRegistry.QUIVER_ITEM.get()) {
            if (this.quiver != null) {
                this.spawnAtLocation(quiver);
            }
            this.setQuiver(stack);
            this.quiverDropChance = 1;
            return true;
        }
        return super.equipItemIfPossible(stack);
    }
}