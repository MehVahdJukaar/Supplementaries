package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.inventory.container.GrindstoneContainer$4"})
public abstract class GrindstoneTestSlotMixin {


    @Inject(method = "getExperienceFromItem(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void getExperienceFromItem(ItemStack stack, CallbackInfoReturnable<Integer> cir){
        if(stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE){
            cir.setReturnValue(50);
            cir.cancel();
        }
    }
}
