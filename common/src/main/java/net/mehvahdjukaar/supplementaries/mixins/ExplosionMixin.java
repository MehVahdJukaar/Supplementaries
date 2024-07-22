package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.events.ClientEvents;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow
    @Final
    public Level level;

    @Inject(method = "finalizeExplosion", at = @At("HEAD"))
    public void explode(CallbackInfo ci) {
        if (level.isClientSide) ClientEvents.onExplosion((Explosion) (Object) this);
    }
}
