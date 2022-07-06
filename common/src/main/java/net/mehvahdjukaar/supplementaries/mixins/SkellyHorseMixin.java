package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.util.ICustomDataHolder;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(SkeletonHorse.class)
public abstract class SkellyHorseMixin extends AbstractHorse implements ICustomDataHolder {

    public boolean getVariable() {
        return this.isConverting();
    }

    public void setVariable(boolean val) {
    }

    @Unique
    private int fleshCount = 0;
    @Unique
    private int conversionTime = -1;

    protected SkellyHorseMixin(EntityType<? extends AbstractHorse> p_i48563_1_, Level p_i48563_2_) {
        super(p_i48563_1_, p_i48563_2_);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(CompoundTag compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt("FleshCount", this.fleshCount);
        compoundNBT.putInt("ConversionTime", this.conversionTime);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundTag compoundNBT, CallbackInfo ci) {
        this.fleshCount = compoundNBT.getInt("FleshCount");
        this.conversionTime = compoundNBT.getInt("ConversionTime");
    }

    @Inject(method = "mobInteract", at = @At(value = "HEAD"), cancellable = true)
    public void mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (ServerConfigs.cached.ZOMBIE_HORSE && this.isTamed() && !this.isBaby()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() == Items.ROTTEN_FLESH && fleshCount < ServerConfigs.cached.ZOMBIE_HORSE_COST) {
                this.feedRottenFlesh(player, hand, stack);
                cir.cancel();
                cir.setReturnValue(InteractionResult.sidedSuccess(player.level.isClientSide));
            }
        }
    }


    /*
    @Redirect(method ="mobInteract",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"
            ))
    public boolean mobInteract(ItemStack stack) {
        boolean empty = stack.isEmpty();

        if(!empty && stack.getItem() == Items.ROTTEN_FLESH){
            fleshCount
        }
        return empty;
    }*/

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
        this.fleshCount++;

        if (this.fleshCount >= ServerConfigs.cached.ZOMBIE_HORSE_COST) {
            this.conversionTime = 200;
            this.level.broadcastEntityEvent(this, (byte) 16);
        }

        if (!player.isCreative()) {
            stack.shrink(1);
        }
    }

    private boolean isConverting() {
        return this.conversionTime > 0;
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
            for (EquipmentSlot equipmentslottype : EquipmentSlot.values()) {
                ItemStack itemstack = this.getItemBySlot(equipmentslottype);
                if (!itemstack.isEmpty()) {
                    if (EnchantmentHelper.hasBindingCurse(itemstack)) {
                        newHorse.getSlot(equipmentslottype.getIndex() + 300).set(itemstack);
                    } else {
                        double d0 = this.getEquipmentDropChance(equipmentslottype);
                        if (d0 > 1.0D) {
                            this.spawnAtLocation(itemstack);
                        }
                    }
                }
            }
            net.minecraftforge.event.ForgeEventFactory.onLivingConvert(this, newHorse);
        }

        if (!this.isSilent()) {
            this.level.levelEvent(null, 1027, this.blockPosition(), 0);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            if (!this.isSilent()) {
                this.level.playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, this.getSoundSource(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
            }

        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide && this.isAlive() && !this.isNoAi()) {
            if (this.isConverting()) {
                --this.conversionTime;

                if (this.conversionTime == 0) {
                    this.doZombieConversion();
                }
            }
        }
    }


}