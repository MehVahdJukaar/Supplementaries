package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.client.cannon.CannonRiderRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Inject(method = "setPartVisibility", at = @At("TAIL"))
    private void suppl$hideRiderArmorBelowHead(HumanoidModel<?> model, EquipmentSlot slot, CallbackInfo ci) {
        if (slot != EquipmentSlot.HEAD && CannonRiderRenderer.isRenderingFromCannon()) {
            model.setAllVisible(false);
        }
    }
}
