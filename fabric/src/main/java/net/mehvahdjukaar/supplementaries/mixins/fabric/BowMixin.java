package net.mehvahdjukaar.supplementaries.mixins.fabric;

import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BowItem.class)
public abstract class BowMixin {

    @Inject(method = "releaseUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void shrinkQuiverArrow(ItemStack stack, Level level, LivingEntity shooter,
                                   int timeCharged, CallbackInfo ci, Player player,
                                   boolean bl, ItemStack arrowStack, int i, float f, boolean bl2) {
        if (!player.getInventory().hasAnyMatching(s -> s == arrowStack)) {
            var q = QuiverItem.getQuiver(shooter);
            if (!q.isEmpty()) {
                var data = QuiverItem.getQuiverContent(q);
                if (data != null) data.consumeSelected();
            }
        }
    }
}
