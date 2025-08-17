package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxSlot.class)
public class ShulkerSlotMixin {

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    public void supp$preventInsertion(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.is(ModTags.SHULKER_BLACKLIST_TAG))
            cir.setReturnValue(false);
    }
}
