package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.world.inventory.GrindstoneMenu$4"})
public abstract class GrindstoneTestSlotMixin {


    @Inject(method = {"getExperienceFromItem"}, at = @At("HEAD"), cancellable = true)
    private void getExperienceFromItem(ItemStack stack, CallbackInfoReturnable<Integer> cir){
        Item i = stack.getItem();
        if(i == Items.ENCHANTED_GOLDEN_APPLE || i == ModRegistry.BOMB_BLUE_ITEM.get()){
            cir.setReturnValue(50);
        }
    }
}
