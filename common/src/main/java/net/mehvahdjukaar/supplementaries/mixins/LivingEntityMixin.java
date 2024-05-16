package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
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
    @Nullable
    public abstract MobEffectInstance getEffect(MobEffect pPotion);

    @Inject(method = "getJumpBoostPower", at = @At("RETURN"), cancellable = true)
    private void suppl$checkOverencumbered(CallbackInfoReturnable<Float> cir) {
        var effect = this.getEffect(ModRegistry.OVERENCUMBERED.get());
        if (effect != null && effect.getAmplifier() > 0) cir.setReturnValue(cir.getReturnValue() - 0.1f);
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
                                          @Local LocalRef<ItemStack> food) {
        if (stack.getItem() instanceof LunchBoxItem li) {
            var data = li.getData(stack);
            if (data.canEatFrom()) {
                food.set(data.getSelected());
            }
        }
    }
}
