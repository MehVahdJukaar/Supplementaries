package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.ModTags;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxTileEntity.class)
public abstract class ShulkerBoxTileEntityMixin {


    @Inject(method = "canPlaceItemThroughFace", at = @At("HEAD"), cancellable = true)
    public void canInsertItem(int index, ItemStack itemStackIn, Direction direction, CallbackInfoReturnable<Boolean> info) {
        if (itemStackIn.getItem().is(ModTags.SHULKER_BLACKLIST_TAG)) {
            info.setReturnValue(false);
        }
    }
}
