package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.CannonBoatRenderer;
import net.mehvahdjukaar.supplementaries.common.entities.CannonBoatEntity;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoatRenderer.class)
public abstract class BoatRendererMixin {

    @Shadow @Deprecated public abstract ResourceLocation getTextureLocation(Boat entity);

    // here so we use the vanilla render code with another texture for best compatibility. alternative was copy pasting that
    @ModifyExpressionValue(method = "render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
    at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Pair;getFirst()Ljava/lang/Object;"))
    public Object supp$changeTexture(Object original, Boat boat){
        if(boat.getClass() == CannonBoatEntity.class ){
           return this.getTextureLocation(boat);
        }
        return original;
    }
}
