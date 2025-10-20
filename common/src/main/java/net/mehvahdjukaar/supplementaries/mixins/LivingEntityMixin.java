package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.supplementaries.common.entities.data.SlimedData;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.Holder;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    protected LivingEntityMixin(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Shadow
    public abstract boolean onClimbable();

    @Shadow
    public abstract boolean isSuppressingSlidingDownLadder();

    @Shadow
    public abstract @Nullable MobEffectInstance getEffect(Holder<MobEffect> effect);

    @ModifyReturnValue(method = "getJumpBoostPower", at = @At("RETURN"))
    private float suppl$checkOverencumbered(float original) {
        var effect = this.getEffect(ModRegistry.OVERENCUMBERED.getHolder());
        if ((effect != null && effect.getAmplifier() > 0)) {
            original -= 0.1f;
        }
        // yes they stack
        LivingEntity self = (LivingEntity) (Object) this;
        SlimedData data = ModRegistry.SLIMED_DATA.getOrCreate(self);
        if (data.isSlimed()) {
            var mode = CommonConfigs.Tweaks.HINDERS_JUMP.get();
            if (mode.isOn(this.level())) {
                original -= 0.1f;
            }
        }
        return original;
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "handleOnClimbable", at = @At("HEAD"), cancellable = true)
    private void suppl$checkOnRope(Vec3 motion, CallbackInfoReturnable<Vec3> info) {
        if (this.onClimbable() && CommonConfigs.Functional.ROPE_SLIDE.get()) {
            BlockState b = this.getBlockStateOn();
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
        var data = stack.get(ModComponents.LUNCH_BASKET_CONTENT.get());
        if (data != null && data.canEatFrom()) {
            food.set(data.getSelected());
        }

    }

    // yes thiscould be called with forge event instead. doesn't make much difference really. needed for fabric and couldn't be another make a fabric only mixin
    @Inject(method = "tick", at = @At("HEAD"))
    private void suppl$slimeTick(CallbackInfo ci) {
        LivingEntity le = (LivingEntity) (Object) this;
        SlimedData data = ModRegistry.SLIMED_DATA.getOrCreate(le);
        data.tick(le);
    }

}
