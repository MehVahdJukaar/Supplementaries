package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "setup", at = @At(value = "INVOKE",
            shift = At.Shift.BEFORE,
            target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"),
            cancellable = true)
    public void supp$setupCannonCamera(BlockGetter level, Entity entity, boolean detached,
                                                  boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (CannonController.setupCamera((Camera) (Object) this,
                level, entity, detached, thirdPersonReverse, partialTick)) {
            ci.cancel();
        }
    }
}
