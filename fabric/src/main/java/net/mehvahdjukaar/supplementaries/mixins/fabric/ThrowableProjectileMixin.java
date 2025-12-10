package net.mehvahdjukaar.supplementaries.mixins.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.supplementaries.common.events.ServerEvents;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrowableProjectile.class)
public class ThrowableProjectileMixin {

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)Lnet/minecraft/world/phys/HitResult;"))
    public HitResult supp$onHit(HitResult original) {
        if (original.getType() != HitResult.Type.MISS) {
            ServerEvents.onProjectileImpact((Projectile) (Object) this, original);
        }
        return original;
    }
}
