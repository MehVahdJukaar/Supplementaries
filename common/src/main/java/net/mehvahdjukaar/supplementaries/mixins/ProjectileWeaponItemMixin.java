package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
