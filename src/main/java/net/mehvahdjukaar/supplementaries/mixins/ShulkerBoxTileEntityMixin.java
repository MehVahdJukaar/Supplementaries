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


    @Inject(method = "canInsertItem", at = @At("HEAD"), cancellable = true)
    public void canInsertItem(int index, ItemStack itemStackIn, Direction direction, CallbackInfoReturnable<Boolean> info ) {
        if(ModTags.isTagged(ModTags.SHULKER_BLACKLIST_TAG,itemStackIn.getItem()))
            info.setReturnValue(false);
    }
}
