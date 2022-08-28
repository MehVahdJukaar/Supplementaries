package net.mehvahdjukaar.supplementaries.mixins.fabric;

import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class BowMixin {

    @Inject(method = "releaseUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static void shrinkQuiverArrow(ItemStack stack, Level level, LivingEntity livingEntity,
                                          int timeCharged, CallbackInfo ci) {
        var q = QuiverItem.getQuiver(livingEntity);
        if (q != null) {
            var data = QuiverItem.getQuiverData(q);
            if (data != null) data.consumeArrow();
        }
    }
}
