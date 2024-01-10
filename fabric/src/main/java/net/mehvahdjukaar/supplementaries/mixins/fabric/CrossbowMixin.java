package net.mehvahdjukaar.supplementaries.mixins.fabric;

import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public abstract class CrossbowMixin {

    @Inject(method = "loadProjectile",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"))
    private static void shrinkQuiverArrow(LivingEntity shooter, ItemStack crossbowStack,
                                          ItemStack ammoStack, boolean hasAmmo, boolean isCreative,
                                          CallbackInfoReturnable<Boolean> cir) {
        //Very hacky. Consume if the arrow isnt from inventory
        if(shooter instanceof Player p && !p.getInventory().hasAnyMatching(i->i==ammoStack)) {
            var q = QuiverItem.getQuiver(shooter);
            if (!q.isEmpty()) {
                var data = QuiverItem.getQuiverData(q);
                if (data != null) data.consumeArrow();
            }
        }
    }
}