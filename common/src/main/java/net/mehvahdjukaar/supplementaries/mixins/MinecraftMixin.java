package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.items.LunchBoxItem;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.mehvahdjukaar.supplementaries.common.network.ServerBoundLunchBoxRightClickedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow @Nullable
    public LocalPlayer player;

    //cancel rope slide down sound
    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;",
            shift = At.Shift.BEFORE))
    private void suppl$switchLunchBoxMode(CallbackInfoReturnable<Boolean> cir) {
        if (this.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof LunchBoxItem) {
            ModNetwork.CHANNEL.sendToServer(new ServerBoundLunchBoxRightClickedPacket(InteractionHand.MAIN_HAND));
        }
    }

}
