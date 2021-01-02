package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.minecraft.inventory.container.ShulkerBoxSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxSlot.class)
public class ShulkerBoxSlotMixin {

    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
    public void isItemValid(ItemStack itemStackIn, CallbackInfoReturnable<Boolean> info ) {
        if(itemStackIn.getItem() instanceof SackItem) info.setReturnValue(false);
    }
}
