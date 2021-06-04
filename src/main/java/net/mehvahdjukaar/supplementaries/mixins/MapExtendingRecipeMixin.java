package net.mehvahdjukaar.supplementaries.mixins;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.MapExtendingRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MapExtendingRecipe.class)
public abstract class MapExtendingRecipeMixin {


    @Redirect(method ="matches",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
                    ordinal = 1))
    private boolean matches(ItemStack original, CraftingInventory inventory, World world) {
        CompoundNBT compoundnbt = original.getTag();
        if (compoundnbt != null && compoundnbt.contains("CustomDecorations", 9)) {
            return true;
        }
        return false;
    }

}