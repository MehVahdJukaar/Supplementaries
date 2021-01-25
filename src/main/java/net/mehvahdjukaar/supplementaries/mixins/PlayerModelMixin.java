package net.mehvahdjukaar.supplementaries.mixins;


import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> extends BipedModel<T> {
    public PlayerModelMixin(float p_i1148_1_) {
        super(p_i1148_1_);
    }

    @Shadow
    @Final
    public ModelRenderer bipedLeftArmwear;

    @Shadow
    @Final
    public ModelRenderer bipedRightArmwear;

    @Inject(at = @At("TAIL"), method = "setRotationAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V")
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo info) {
       /*
        if (entityIn.getPrimaryHand() == HandSide.RIGHT) {
            this.eatingAnimationRightHand(Hand.MAIN_HAND, entityIn, ageInTicks);
            this.eatingAnimationLeftHand(Hand.OFF_HAND, entityIn, ageInTicks);
        } else {
            this.eatingAnimationRightHand(Hand.OFF_HAND, entityIn, ageInTicks);
            this.eatingAnimationLeftHand(Hand.MAIN_HAND, entityIn, ageInTicks);
        }*/

        //this.bipedRightArm.copyModelAngles(this.bipedHead);
        //this.bipedRightArm.rotateAngleY = -0.5F;
        //this.bipedRightArm.rotateAngleX = -1.3F;
        //this.bipedRightArm.rotateAngleZ = MathHelper.cos(ageInTicks) * 0.1F;
        //this.rightArmPose=ArmPose.CROSSBOW_HOLD;





        //this.do3rdPersonMapFilledAnim(Hand.MAIN_HAND, entityIn);
    }


/*public void do3rdPersonMapFilledAnim(Hand hand, LivingEntity entity) {
   ItemStack itemstack = entity.getHeldItem(hand);
   if (itemstack.getItem() instanceof FilledMapItem) {
           this.bipedRightArm.rotateAngleX = -1.0F;
           this.bipedRightArm.rotateAngleY = (-(float)Math.PI / 6.0F);
           this.bipedLeftArm.rotateAngleX = -1.0F;
           this.bipedLeftArm.rotateAngleY = (-(float)Math.PI / -6.0F);
           this.bipedRightArmwear.copyModelAngles(bipedRightArm);
           this.bipedLeftArmwear.copyModelAngles(bipedLeftArm);
   }
}*/
}

