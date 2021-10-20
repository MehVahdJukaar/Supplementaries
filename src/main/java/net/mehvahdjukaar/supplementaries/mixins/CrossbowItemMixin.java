package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.items.RopeArrowItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin({CrossbowItem.class})
public abstract class CrossbowItemMixin {

    @ModifyVariable(method = "tryLoadProjectiles", at = @At("STORE"), ordinal = 1)
    private static int tryLoadProjectiles(int original, LivingEntity entity, ItemStack stack) {
        if (original > 1 && entity.getProjectile(stack).getItem() instanceof RopeArrowItem) return 1;
        return original;
    }
}
