package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.util.ICustomDataHolder;
import net.mehvahdjukaar.supplementaries.configs.ServerConfigs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.SkeletonHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

@Mixin(SkeletonHorseEntity.class)
public abstract class SkeletonHorseMixin extends AbstractHorseEntity implements ICustomDataHolder {

    public boolean getVariable() {
        return this.isConverting();
    }


    public void setVariable(boolean val) {
    }


    private int fleshCount = 0;
    private int conversionTime = -1;

    protected SkeletonHorseMixin(EntityType<? extends AbstractHorseEntity> p_i48563_1_, World p_i48563_2_) {
        super(p_i48563_1_, p_i48563_2_);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveData(CompoundNBT compoundNBT, CallbackInfo ci) {
        compoundNBT.putInt("FleshCount", this.fleshCount);
        compoundNBT.putInt("ConversionTile", this.conversionTime);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveData(CompoundNBT compoundNBT, CallbackInfo ci) {
        this.fleshCount = compoundNBT.getInt("FleshCount");
        this.conversionTime = compoundNBT.getInt("ConversionTile");
    }

    @Inject(method = "mobInteract", at = @At(value = "HEAD"), cancellable = true)
    public void mobInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResultType> cir) {
        if (ServerConfigs.cached.ZOMBIE_HORSE && this.isTamed() && !this.isBaby()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() == Items.ROTTEN_FLESH && fleshCount < ServerConfigs.cached.ZOMBIE_HORSE_COST) {
                this.feedRottenFlesh(player, hand, stack);
                cir.setReturnValue(ActionResultType.sidedSuccess(player.level.isClientSide));
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

    public void setEating() {
        try {
            Method m = ObfuscationReflectionHelper.findMethod(AbstractHorseEntity.class, "func_110266_cB");
            m.setAccessible(true);
            m.invoke(this);
        } catch (Exception ignored) {
        }
    }

    public void feedRottenFlesh(PlayerEntity player, Hand hand, ItemStack stack) {
        float heal = 0.5f;
        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(heal);
        }

        this.setEating();
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
        float yHeadRotO = this.yHeadRotO;
        AbstractHorseEntity newHorse = this.convertTo(EntityType.ZOMBIE_HORSE, true);
        if (newHorse != null) {

            newHorse.yBodyRot = yBodyRot;
            newHorse.yHeadRot = yHeadRot;
            //newHorse.yBodyRotO = yBodyRotO;
            newHorse.yHeadRotO = yHeadRotO;

            newHorse.setOwnerUUID(this.getOwnerUUID());
            newHorse.setTamed(this.isTamed());

            newHorse.addEffect(new EffectInstance(Effects.CONFUSION, 200, 0));

            if (this.isSaddled()) {
                newHorse.equipSaddle(null);
            }
            for (EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
                ItemStack itemstack = this.getItemBySlot(equipmentslottype);
                if (!itemstack.isEmpty()) {
                    if (EnchantmentHelper.hasBindingCurse(itemstack)) {
                        newHorse.setSlot(equipmentslottype.getIndex() + 300, itemstack);
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


    @OnlyIn(Dist.CLIENT)
    public void handleEntityEvent(byte p_70103_1_) {
        if (p_70103_1_ == 16) {
            if (!this.isSilent()) {
                this.level.playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, this.getSoundSource(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
            }

        } else {
            super.handleEntityEvent(p_70103_1_);
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