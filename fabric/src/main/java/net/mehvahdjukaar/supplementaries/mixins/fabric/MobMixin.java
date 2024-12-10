package net.mehvahdjukaar.supplementaries.mixins.fabric;

import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;registerGoals()V",
            shift = At.Shift.AFTER))
    private void dummy$addExtraGoals(EntityType entityType, Level level, CallbackInfo ci) {
        if (level instanceof ServerLevel sl)
            ServerEvents.onEntityLoad(this, sl);
    }
}
