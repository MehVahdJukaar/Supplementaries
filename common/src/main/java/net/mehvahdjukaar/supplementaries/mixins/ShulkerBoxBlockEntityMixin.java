package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.setup.ModTags;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin {


    @Inject(method = "canPlaceItemThroughFace", at = @At("HEAD"), cancellable = true)
    public void canInsertItem(int index, ItemStack itemStackIn, Direction direction, CallbackInfoReturnable<Boolean> info ) {
        if(itemStackIn.is(ModTags.SHULKER_BLACKLIST_TAG))
            info.setReturnValue(false);
    }
}
