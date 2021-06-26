package net.mehvahdjukaar.supplementaries.mixins;


import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.inventory.container.GrindstoneContainer$2", "net.minecraft.inventory.container.GrindstoneContainer$3"})
public abstract class GrindstoneInputSlotMixin {

    @Inject(method = "mayPlace(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void mayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
