package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.CartographyTableMenu$4")
public abstract class CartographyTableInputSlotMixin {

    @Inject(method = {"mayPlace(Lnet/minecraft/world/item/ItemStack;)Z"}, at = @At("HEAD"), cancellable = true)
    private void mayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ModRegistry.ANTIQUE_INK.get())) {
            cir.setReturnValue(true);
        }
    }

}
