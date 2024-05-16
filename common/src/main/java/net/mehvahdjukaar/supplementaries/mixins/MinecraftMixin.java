package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundLunchBoxRightClickedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    //cancel rope slide down sound
    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;",
            shift = At.Shift.BEFORE))
    private void suppl$switchLunchBoxMode(CallbackInfoReturnable<Boolean> cir, @Local ItemStack item) {
        if (item.getItem() instanceof LunchBoxItem) {
            ModNetwork.CHANNEL.sendToServer(new ServerBoundLunchBoxRightClickedPacket(InteractionHand.MAIN_HAND));
        }
    }

}
