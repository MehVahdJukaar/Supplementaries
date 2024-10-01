package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponMixin {

    // use use ammo
    @WrapOperation(method = "useAmmo",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;")
    )
    private static ItemStack supp$shrinkQuiverArrow(ItemStack ammo, int amount, Operation<ItemStack> original,
                                                    @Local(argsOnly = true) LivingEntity shooter) {
        //check if has exact match in iv. if it does it means it wasnt a quiver arrow
        if (shooter instanceof Player p && !p.getInventory().hasAnyMatching(s -> s == ammo)) {
            var q = QuiverItem.getActiveQuiver(shooter);
            if (!q.isEmpty()) {
                var data = q.get(ModComponents.QUIVER_CONTENT.get());
                if (data != null) {
                    ItemStack selectedCopy = data.getSelected();
                    var mutable = data.toMutable();
                    mutable.consumeSelected(amount);
                    q.set(ModComponents.QUIVER_CONTENT.get(), mutable.toImmutable());
                    return selectedCopy;
                }
            }
        }
        return original.call(ammo, amount);
    }
}
