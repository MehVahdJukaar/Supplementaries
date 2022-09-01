package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
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
public class ProjectileWeaponItemMixin {

    @Inject(method = "getHeldProjectile",
            at = @At("HEAD"),
            cancellable = true)
    private static void getProjectileInQuiver(LivingEntity shooter, Predicate<ItemStack> isAmmo, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = shooter.getItemInHand(InteractionHand.OFF_HAND);
        if (stack.getItem() instanceof QuiverItem) {
            QuiverItem.IQuiverData data = QuiverItem.getQuiverData(stack);
            if (data != null) {
                ItemStack arrow = data.getSelected(isAmmo);
                if (arrow.isEmpty()) cir.setReturnValue(arrow);
            }
        }
        stack = shooter.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.getItem() instanceof QuiverItem) {
            QuiverItem.IQuiverData data = QuiverItem.getQuiverData(stack);
            if (data != null) {
                ItemStack arrow = data.getSelected(isAmmo);
                if (arrow.isEmpty()) cir.setReturnValue(arrow);
            }
        }
    }
}
