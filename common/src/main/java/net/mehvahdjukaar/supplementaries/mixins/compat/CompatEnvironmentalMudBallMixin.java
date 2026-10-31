package net.mehvahdjukaar.supplementaries.mixins.compat;

import net.mehvahdjukaar.supplementaries.common.entities.data.SlimedData;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.integration.EnvironmentalCompat;
import net.mehvahdjukaar.supplementaries.reg.ModData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.teamabnormals.environmental.common.entity.projectile.ThrownMudBall")
public abstract class CompatEnvironmentalMudBallMixin extends ThrowableItemProjectile {

    protected CompatEnvironmentalMudBallMixin(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }

    @Inject(method = "onHit", at = @At("HEAD"))
    protected void supp$applyMud(HitResult result, CallbackInfo ci) {
        if (level().isClientSide || !CommonConfigs.Tweaks.MUD_BALL_SLIMED.get()) return;
        if (result instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity le && le.attackable()) {
            if (le instanceof Pig pig && EnvironmentalCompat.getsMuddyFromMudBall(pig)) return;
            SlimedData data = ModData.SLIMED_DATA.getOrCreate(le);
            data.setSlimedTicks(le, CommonConfigs.Tweaks.MUD_BALL_DURATION.get(), SlimedData.Type.MUD);
        }
    }

}