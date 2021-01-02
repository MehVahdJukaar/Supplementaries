package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.items.SackItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderChestTileEntity.class)
public class EnderChestTileEntityMixin {

    @Inject(method = "canInsertItem", at = @At("HEAD"), cancellable = true)
    public void canInsertItem(int index, ItemStack itemStackIn, Direction direction, CallbackInfoReturnable<Boolean> info ) {
        if(itemStackIn.getItem() instanceof SackItem) info.setReturnValue(false);
    }
}
