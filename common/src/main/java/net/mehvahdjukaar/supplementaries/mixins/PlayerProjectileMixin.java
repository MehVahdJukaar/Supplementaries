package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerProjectileMixin extends LivingEntity {

    protected PlayerProjectileMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    // a bit more efficent than using the event. stil we might want to switch to that
    @Inject(method = "getProjectile",
            at = @At(value = "INVOKE_ASSIGN", target =  "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z",
                    shift = At.Shift.BEFORE),
            cancellable = true
    )
    private void supp$getQuiverProjectile(ItemStack weaponStack, CallbackInfoReturnable<ItemStack> cir,
                                          @Local(ordinal = 1) ItemStack itemInSlot, @Local Predicate<ItemStack> supporterArrows) {
        if (!CommonConfigs.Tools.QUIVER_CURIO_ONLY.get()) {
            var data = itemInSlot.get(ModComponents.QUIVER_CONTENT.get());
            if (data != null) {
                ItemStack arrow = data.getSelected(supporterArrows);
                if (!arrow.isEmpty()) cir.setReturnValue(arrow);
            }
        }
    }
}