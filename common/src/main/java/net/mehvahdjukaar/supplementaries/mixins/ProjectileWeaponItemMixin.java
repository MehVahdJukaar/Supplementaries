package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.items.RopeArrowItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ProjectileWeaponItem.class)
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
            if (shooter instanceof IQuiverEntity quiverEntity) { //client only
                quiverStack = quiverEntity.supplementaries$getQuiver();
            } else {
                stack = shooter.getItemInHand(InteractionHand.MAIN_HAND);
                if (stack.getItem() instanceof QuiverItem) {
                    quiverStack = stack;
                } else if (shooter instanceof ServerPlayer sp) {
                    //server side curio stuff
                    quiverStack = CompatHandler.getQuiverFromModsSlots(sp).get(sp);
                }
            }
        }
        if (quiverStack != null) {
            var data = quiverStack.get(ModComponents.QUIVER_CONTENT.get());
            if (data != null) {
                ItemStack arrow = data.getSelected(isAmmo);
                if (!arrow.isEmpty()) cir.setReturnValue(arrow);
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

    @ModifyVariable(method = "draw", at = @At("STORE"), ordinal = 1)
    private static int tryLoadProjectiles(int original, @Local(argsOnly = true) LivingEntity entity,
                                          @Local(ordinal = 2) ItemStack ammo) {
        if (original > 1 && ammo.getItem() instanceof RopeArrowItem) return 1;
        return original;
    }

}
