package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.items.FlagItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.inventory.container.LoomContainer$3")
public abstract class LoomInputSlotMixin  {


    @Inject(method = "mayPlace(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void mayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof FlagItem) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }


}
