package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.moonlight.api.integration.IrisCompat;
import net.mehvahdjukaar.moonlight.api.misc.OptionalMixin;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.supplementaries.common.block.IConvertableHorse;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OptionalMixin(value = "com.github.alexthe668.domesticationinnovation.DomesticationMod", classLoaded = false)
@Mixin(ZombieHorse.class)
public abstract class ZombieHorseMixin extends AbstractHorse implements IConvertableHorse {

    @Unique
    private static final int CONV_TIME = 4600;

    @Unique
    private int supplementaries$conversionTime = -1;

    protected ZombieHorseMixin(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean dismountsUnderwater() {
        return false;
    }
    //called server side. needs syncing with entity event

    @Unique
    public void supp$startConverting() {
        if (!this.supp$isConverting()) {
            this.supplementaries$conversionTime = CONV_TIME;
            this.level().broadcastEntityEvent(this, EntityEvent.ZOMBIE_CONVERTING);
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, CONV_TIME, 2));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ConversionTime", this.supplementaries$conversionTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundNBT) {
        super.readAdditionalSaveData(compoundNBT);
        this.supplementaries$conversionTime = compoundNBT.getInt("ConversionTime");
    }

    public boolean supp$isConverting() {
        return this.supplementaries$conversionTime > 0;
    }

    @Unique
    private void supp$doHorseConversion() {

        float yBodyRot = this.yBodyRot;
        float yHeadRot = this.yHeadRot;
        float yBodyRotO = this.yBodyRotO;
        float yHeadRotO = this.yHeadRotO;
        AbstractHorse newHorse = this.convertTo(EntityType.HORSE, true);
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
    public void handleEntityEvent(byte pId) {
        if (pId == EntityEvent.ZOMBIE_CONVERTING) {
            this.supplementaries$conversionTime = CONV_TIME;
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ZOMBIE_VILLAGER_CURE, this.getSoundSource(), 1.0F + this.random.nextFloat(), this.random.nextFloat() * 0.7F + 0.3F, false);
            }
        }
        else super.handleEntityEvent(pId);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.isAlive() && !this.isNoAi()) {
            if (this.supp$isConverting()) {
                --this.supplementaries$conversionTime;

                if (this.supplementaries$conversionTime == 0) {
                    this.supp$doHorseConversion();
                }
            }
        }
    }

    @Inject(method = "mobInteract", at = @At(value = "HEAD"), cancellable = true)
    public void interact(Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (itemstack.is(Items.GOLDEN_CARROT) && this.hasEffect(MobEffects.WEAKNESS) && CommonConfigs.Tweaks.ZOMBIE_HORSE_CONVERSION.get()) {
            if (!pPlayer.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
            Level level = this.level();

            this.eat(level, itemstack);
            if (!level.isClientSide) {
                this.supp$startConverting();
            }

            cir.cancel();
            cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
        }
    }
}
