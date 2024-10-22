package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.network.SyncEquippedQuiverPacket;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Skeleton.class)
public abstract class SkeletonMixin extends AbstractSkeleton implements IQuiverEntity {
    //frick it going full mixin here. I could have used caps and spawn events...

    //server
    @NotNull
    @Unique
    private ItemStack supplementaries$quiver = ItemStack.EMPTY;
    @Unique
    private float supplementaries$quiverDropChance = 0.6f;

    protected SkeletonMixin(EntityType<? extends AbstractSkeleton> entityType, Level level) {
        super(entityType, level);
    }

    // here since we have to add a mixin anyways. tho we could have used a capability but then again freking fabric...
    @Inject(method = "dropCustomDeathLoot", at = @At("TAIL"))
    protected void supp$dropQuiver(ServerLevel serverLevel, DamageSource damageSource, boolean hitByPlayer, CallbackInfo ci) {
        if (!this.supplementaries$quiver.isEmpty()) {
            //same code as Mob super call for armor
            ItemStack itemStack = this.supplementaries$quiver;

            float dropChance = supplementaries$quiverDropChance;
            if (dropChance != 0) {

                Entity damagingEntity = damageSource.getEntity();
                if (damagingEntity instanceof LivingEntity le) {
                    dropChance = EnchantmentHelper.processEquipmentDropChance(serverLevel, le, damageSource, dropChance);
                }
                boolean alwaysDrop = dropChance > 1.0F;
                if (!EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
                        && (hitByPlayer || alwaysDrop) && this.random.nextFloat() < dropChance) {
                    this.spawnAtLocation(itemStack);
                    this.supplementaries$quiver = ItemStack.EMPTY;
                }
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (!this.supplementaries$quiver.isEmpty()) {
            compound.put("Quiver", supplementaries$quiver.save(level().registryAccess(), new CompoundTag()));
            compound.putFloat("QuiverDropChance", supplementaries$quiverDropChance);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("Quiver")) {
            this.supplementaries$setQuiver(ItemStack.parseOptional(level().registryAccess(), compound.getCompound("Quiver")));
            this.supplementaries$quiverDropChance = compound.getFloat("QuiverDropChance");
        }
    }

    @Override
    public ItemStack supplementaries$getQuiver() {
        return supplementaries$quiver;
    }

    @Override
    public void supplementaries$setQuiver(ItemStack quiver) {
        this.supplementaries$quiver = quiver;
        if (!level().isClientSide) {
            //only needed when entity is alraedy spawned
            NetworkHelper.sendToAllClientPlayersTrackingEntity(this,
                    new SyncEquippedQuiverPacket( this));
        }
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        if (this.supplementaries$quiver.isEmpty() && stack.getItem() == ModRegistry.QUIVER_ITEM.get()) return true;
        return super.wantsToPickUp(stack);
    }

    @Override
    public ItemStack equipItemIfPossible(ItemStack stack) {
        // no quiver swapping to prevent drop exploits
        if (stack.getItem() == ModRegistry.QUIVER_ITEM.get() && supplementaries$quiver.isEmpty()) {
            this.supplementaries$setQuiver(stack);
            this.supplementaries$quiverDropChance = 1;
            return stack;
        }
        return super.equipItemIfPossible(stack);
    }
}