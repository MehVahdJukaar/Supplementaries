package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.entities.ICannonRider;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void suppl$poseForFlight(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                     float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof ICannonRider rider) || !rider.supplementaries$getCannonFlight().isFlying()) {
            return;
        }
        PlayerModel<?> model = (PlayerModel<?>) (Object) this;

        //renderer already tilts the body
        model.head.setRotation(-Mth.HALF_PI, 0, 0);
        //superman
        model.rightArm.setRotation(Mth.PI, 0, -0.2f);
        model.leftArm.setRotation(Mth.PI, 0, 0.2f);
        float flutter = Mth.sin(ageInTicks * 0.8f) * 0.08f;
        model.rightLeg.setRotation(flutter, 0, 0.1f);
        model.leftLeg.setRotation(-flutter, 0, -0.1f);

        model.hat.copyFrom(model.head);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
    }
}
