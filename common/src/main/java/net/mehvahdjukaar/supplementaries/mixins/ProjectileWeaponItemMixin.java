package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.mehvahdjukaar.supplementaries.common.entities.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.items.RopeArrowItem;
import net.mehvahdjukaar.supplementaries.common.items.SlingshotItem;
import net.mehvahdjukaar.supplementaries.common.items.components.QuiverContent;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(value = ProjectileWeaponItem.class, priority = 1400)
public abstract class ProjectileWeaponItemMixin {

    @Inject(method = "getHeldProjectile",
            at = @At("HEAD"),
            cancellable = true)
    //checks everything except inventory. High priority ones. Other ones are done in PlayerProjectileMixin
    private static void supp$getProjectileInQuiver(LivingEntity shooter, Predicate<ItemStack> isAmmo, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = shooter.getItemInHand(InteractionHand.OFF_HAND);
        if (isAmmo.test(stack)) return; //off-hand always has priority
        ItemStack quiverStack = null;
        if (stack.getItem() instanceof QuiverItem) {
            quiverStack = stack;
        }
        if (quiverStack == null) {
            if (shooter instanceof IQuiverEntity quiverEntity) {
                quiverStack = quiverEntity.supplementaries$getQuiver();
            } else {
                stack = shooter.getItemInHand(InteractionHand.MAIN_HAND);
                if (stack.getItem() instanceof QuiverItem) {
                    quiverStack = stack;
                }
            }
        }
        if (quiverStack != null) {
            var data = quiverStack.get(ModComponents.QUIVER_CONTENT.get());
            if (data != null) {
                ItemStack arrow = data.getSelected(isAmmo);
                if (!arrow.isEmpty()) cir.setReturnValue(arrow.copyWithCount(1));
            }
        }
    }


    // use use ammo
    @WrapOperation(method = "useAmmo",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;")
    )
    private static ItemStack supp$shrinkQuiverArrow(ItemStack ammo, int amount, Operation<ItemStack> original,
                                                    @Local(argsOnly = true) LivingEntity shooter) {
        //check if has exact match in iv. if it does it means it wasnt a quiver arrow
        if (shooter instanceof Player p && !p.getInventory().hasAnyMatching(s -> s == ammo)) {
            var q = QuiverItem.findActiveQuiver(shooter);
            if (!q.isEmpty()) {
                QuiverContent data = q.get(ModComponents.QUIVER_CONTENT.get());
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

    @WrapOperation(method = "draw",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ProjectileWeaponItem;useAmmo(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Z)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack supp$preventFreeMultishot(
            ItemStack weapon, ItemStack ammo, LivingEntity shooter, boolean intangable, Operation<ItemStack> op,
            @Local(ordinal = 1, argsOnly = true) ItemStack actualAmmo) {
        if (weapon.getItem() instanceof SlingshotItem || actualAmmo.getItem() instanceof RopeArrowItem) {
            return op.call(weapon, actualAmmo, shooter, false);
        }
        return op.call(weapon, ammo, shooter, intangable);
    }

}
