package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.entities.data.SlimedData;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModEntities;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
public abstract class SlimeMixin extends LivingEntity {

    protected SlimeMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract int getSize();

    @Shadow
    public abstract EntityType<? extends Slime> getType();

    @Inject(method = "dealDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Slime;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    public void supp$applySlimedEffect(LivingEntity livingEntity, CallbackInfo ci) {
        if (!CommonConfigs.Tweaks.SLIMED_EFFECT.get()) return;
        if (this.getType().is(ModTags.CAN_SLIME)) {
            SlimedData slimedData = ModEntities.SLIMED_DATA.getOrCreate(livingEntity);
            double chance = this.getSize() * CommonConfigs.Tweaks.SLIMED_PER_SIZE.get();
            if (livingEntity.getRandom().nextDouble() < chance) {
                slimedData.setSlimedTicks(livingEntity, CommonConfigs.Tweaks.SLIME_DURATION.get());
            }
        }

    }
}
