package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.supplementaries.common.entities.ISlimeable;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncSlimedMessage;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ISlimeable {

    @Unique
    int supp$slimedTicks = 0;

    protected LivingEntityMixin(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public int supp$getSlimedTicks() {
        return supp$slimedTicks;
    }

    @Override
    public void supp$setSlimedTicks(int slimed, boolean sync) {
        int old  = this.supp$slimedTicks;
        this.supp$slimedTicks = slimed;
        if (sync && !this.level().isClientSide) {
            if(old <=0){
                // play sound
                this.playSound(ModSounds.SLIME_SPLAT.get(), 1, 1);
            }
            ModNetwork.CHANNEL.sentToAllClientPlayersTrackingEntityAndSelf(this,
                    new ClientBoundSyncSlimedMessage(this.getId(), this.supp$getSlimedTicks()));
        }
    }

    @Shadow
    public abstract boolean onClimbable();

    @Shadow
    public abstract boolean isSuppressingSlidingDownLadder();

    @Shadow
    @Nullable
    public abstract MobEffectInstance getEffect(MobEffect pPotion);

    @ModifyReturnValue(method = "getJumpBoostPower", at = @At("RETURN"))
    private float suppl$checkOverencumbered(float original) {
        var effect = this.getEffect(ModRegistry.OVERENCUMBERED.get());
        if ((effect != null && effect.getAmplifier() > 0)) {
            original -= 0.1f;
        }
        // yes they stack
        if(this.supp$getSlimedTicks() > 0){
            var mode = CommonConfigs.Tweaks.HINDERS_JUMP.get();
            if(mode.isOn(this.level())){
                original -= 0.1f;
            }
        }
        return original;
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "handleOnClimbable", at = @At("HEAD"), cancellable = true)
    private void suppl$checkOnRope(Vec3 motion, CallbackInfoReturnable<Vec3> info) {
        if (this.onClimbable() && CommonConfigs.Functional.ROPE_SLIDE.get()) {
            BlockState b = this.getFeetBlockState();
            if (b.is(ModTags.FAST_FALL_ROPES)) {
                this.fallDistance = 0;
                double x = Mth.clamp(motion.x, -0.15F, 0.15F);
                double z = Mth.clamp(motion.z, -0.15F, 0.15F);
                double y = motion.y();

                if (this.isSuppressingSlidingDownLadder()) {
                    if (y < 0 && ((Object) this) instanceof Player) y = 0;
                }
                info.setReturnValue(new Vec3(x, y, z));
            }
        }
    }

    @Inject(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isUsingItem()Z"))
    private void suppl$eatFromLunchBasket(ItemStack stack, int amount, CallbackInfo ci,
                                          @Local(argsOnly = true) LocalRef<ItemStack> food) {
        if (stack.getItem() instanceof LunchBoxItem li) {
            var data = li.getData(stack);
            if (data.canEatFrom()) {
                food.set(data.getSelected());
            }
        }
    }
}
