package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.block.util.ICustomDataHolder;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({VillagerModel.class})
public abstract class VillagerModelMixin<T extends Entity>  {

    @Final
    @Shadow
    protected ModelPart nose;

    @Final
    @Shadow
    private ModelPart head;
    protected ModelPart nose2;

    //TODO: re add this

    /*
    @Inject(method = {"<init>"}, at = {@At(value = "TAIL")})
    private void init(ModelPart p_171051_, CallbackInfo ci) {
        this.nose2 = (new ModelPart(this)).setTexSize(p_i51059_2_, p_i51059_3_);
        nose2.setPos(0.0F, -2.0F, -5.0F);
        nose2.texOffs(24, 0)
                .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 4.0F, 2.0F, p_i51059_1_);
        this.head.addChild(this.nose2);
        this.nose2.visible = false;
    }


    @Inject(method = {"setupAnim"},
            at = {@At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/merchant/villager/AbstractVillagerEntity;getUnhappyCounter()I"
            )}, cancellable = true)
    private void setupAnim(T villager, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_, CallbackInfo ci) {
        if(villager instanceof ICustomDataHolder) {
            this.nose2.visible = true;
            this.nose.visible = false;
            if (((ICustomDataHolder) villager).getVariable()) {
                this.nose2.xRot = -0.40F + (0.415F * Mth.sin(0.13F * p_225597_4_));
                this.nose2.zRot =  + (0.03F * Mth.sin(0.03F * (p_225597_4_ +0.3f)));
            }else{

                this.nose2.xRot = 0;
                this.nose2.zRot = 0;
            }
        }
    }
    */

}

