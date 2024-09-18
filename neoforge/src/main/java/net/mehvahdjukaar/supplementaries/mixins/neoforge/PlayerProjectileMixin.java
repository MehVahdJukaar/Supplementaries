package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerProjectileMixin extends LivingEntity {

    protected PlayerProjectileMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(locals = LocalCapture.CAPTURE_FAILHARD, method = "getProjectile",
            at = @At(value = "INVOKE_ASSIGN", target =  "Ljava/util/function/Predicate;test(Ljava/lang/Object;)Z",
                    shift = At.Shift.BEFORE),
            cancellable = true
    )
    private void getProjectile(ItemStack weaponStack, CallbackInfoReturnable<ItemStack> cir,
                               Predicate<ItemStack> supporterArrows, ItemStack heldArrow, int slotIndex, ItemStack itemStack) {
        if (itemStack.getItem() instanceof QuiverItem && !CommonConfigs.Tools.QUIVER_CURIO_ONLY.get()) {
            ItemStack arrow = QuiverItem.getQuiverData(itemStack).getSelected(supporterArrows);
            if (!arrow.isEmpty()) cir.setReturnValue(arrow);
        }
    }
}