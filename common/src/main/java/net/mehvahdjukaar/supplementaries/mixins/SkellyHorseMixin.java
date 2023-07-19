package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.block.ICustomDataHolder;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.Nullable;

@Mixin(SkeletonHorse.class)
public abstract class SkellyHorseMixin extends AbstractHorse implements ICustomDataHolder {

    public boolean getVariable() {
        return this.isConverting();
    }

    public void setVariable(boolean val) {
    }

    @Unique
    private int supplementaries$fleshCount = 0;
    @Unique
    private int supplementaries$conversionTime = -1;

    protected SkellyHorseMixin(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(CompoundTag compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt("FleshCount", this.supplementaries$fleshCount);
        compoundNBT.putInt("ConversionTime", this.supplementaries$conversionTime);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag compoundNBT, CallbackInfo ci) {
        this.supplementaries$fleshCount = compoundNBT.getInt("FleshCount");
        this.supplementaries$conversionTime = compoundNBT.getInt("ConversionTime");
    }

    @Inject(method = "mobInteract", at = @At(value = "HEAD"), cancellable = true)
    public void mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (CommonConfigs.Tweaks.ZOMBIE_HORSE.get() && this.isTamed() && !this.isBaby()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() == Items.ROTTEN_FLESH && supplementaries$fleshCount < CommonConfigs.Tweaks.ZOMBIE_HORSE_COST.get()) {
                this.feedRottenFlesh(player, hand, stack);
                cir.cancel();
                cir.setReturnValue(InteractionResult.sidedSuccess(player.level().isClientSide));
            }
        }
    }

    @Override
    @Nullable
    protected SoundEvent getEatingSound() {
        return SoundEvents.HORSE_EAT;
    }

    public void feedRottenFlesh(Player player, InteractionHand hand, ItemStack stack) {
        float heal = 0.5f;
        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(heal);
        }

        this.setEating(true);
        this.supplementaries$fleshCount++;

        if (this.supplementaries$fleshCount >= CommonConfigs.Tweaks.ZOMBIE_HORSE_COST.get()) {
            this.supplementaries$conversionTime = 200;
            this.level().broadcastEntityEvent(this, (byte) 16);
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }

    private boolean isConverting() {
        return this.supplementaries$conversionTime > 0;
    }

    private void doZombieConversion() {

        float yBodyRot = this.yBodyRot;
        float yHeadRot = this.yHeadRot;
        float yBodyRotO = this.yBodyRotO;
        float yHeadRotO = this.yHeadRotO;
        AbstractHorse newHorse = this.convertTo(EntityType.ZOMBIE_HORSE, true);
        if (newHorse != null) {

            newHorse.yBodyRot = yBodyRot;
            newHorse.yHeadRot = yHeadRot;
            //newHorse.yBodyRotO = yBodyRotO;
            newHorse.yHeadRotO = yHeadRotO;

            newHorse.setOwnerUUID(this.getOwnerUUID());
            newHorse.setTamed(this.isTamed());

            newHorse.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));

            if (this.isSaddled()) {
                newHorse.equipSaddle(null);
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack itemstack = this.getItemBySlot(slot);
                if (!itemstack.isEmpty()) {
                    if (EnchantmentHelper.hasBindingCurse(itemstack)) {
                        newHorse.getSlot(slot.getIndex() + 300).set(itemstack);
                    } else {
                        double d0 = this.getEquipmentDropChance(slot);
                        if (d0 > 1.0D) {
                            this.spawnAtLocation(itemstack);
                        }
                    }
                }
            }
            ForgeHelper.onLivingConvert(this, newHorse);
        }

        if (!this.isSilent()) {
            this.level().levelEvent(null, LevelEvent.SOUND_ZOMBIE_CONVERTED, this.blockPosition(), 0);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, this.getSoundSource(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
            }

        } else {
            super.handleEntityEvent(id);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.isAlive() && !this.isNoAi()) {
            if (this.isConverting()) {
                --this.supplementaries$conversionTime;

                if (this.supplementaries$conversionTime <= 0 && ForgeHelper.canLivingConvert(this, EntityType.ZOMBIE_HORSE, (timer) -> this.supplementaries$conversionTime = timer)) {
                    this.doZombieConversion();
                }
            }
        }
    }


}