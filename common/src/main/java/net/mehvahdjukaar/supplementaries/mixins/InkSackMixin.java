package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.AntiqueInkItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.InkSacItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InkSacItem.class)
public class InkSackMixin {

    @Inject(method = "tryApplyToSign", at = @At("HEAD"), cancellable = true)
    public void supp$clearAntiqueInk(Level level, SignBlockEntity signBlockEntity,
                                                boolean bl, Player player, CallbackInfoReturnable<Boolean> cir){
        if(AntiqueInkItem.toggleAntiqueInkOnSigns(level, player, signBlockEntity.getBlockPos(), signBlockEntity, false)){
            cir.setReturnValue(true);
        }
    }
}
