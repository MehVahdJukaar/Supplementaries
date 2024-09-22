package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.block.IAntiquable;
import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BookViewScreen.BookAccess.class)
public abstract class WrittenBookAccessMixin implements IAntiquable {

    @Unique
    private boolean supplementaries$antiqueInk;

    @ModifyReturnValue(method = "fromItem", at = @At("RETURN"))
    private static BookViewScreen.BookAccess supp$checkAntiqueInk(BookViewScreen.BookAccess original,
                                                                  @Local(argsOnly = true) ItemStack stack){
        ((IAntiquable)(Object) original).supplementaries$setAntique(AntiqueInkItem.hasAntiqueInk(stack));
        return original;
    }

    @Override
    public boolean supplementaries$isAntique() {
        return supplementaries$antiqueInk;
    }

    @Override
    public void supplementaries$setAntique(boolean hasInk) {
        this.supplementaries$antiqueInk = hasInk;
    }
}
