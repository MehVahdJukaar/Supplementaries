package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.entities.ISlimeable;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
public abstract class SlimeMixin {

    @Shadow
    public abstract int getSize();

    @Inject(method = "dealDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Slime;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    public void supp$applySlimedEffect(LivingEntity livingEntity, CallbackInfo ci) {
        if (!CommonConfigs.Tweaks.SLIMED_EFFECT.get()) return;
        double chance = this.getSize() * CommonConfigs.Tweaks.SLIMED_PER_SIZE.get();
        if (livingEntity.getRandom().nextDouble() < chance && livingEntity instanceof ISlimeable s) {
            s.supp$setSlimedTicks(CommonConfigs.Tweaks.SLIME_DURATION.get(), true);
        }

    }
}
