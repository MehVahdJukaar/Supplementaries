package net.mehvahdjukaar.supplementaries.mixins;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    public LocalPlayerMixin(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }

    @Inject(method = "hasEnoughImpulseToStartSprinting", at = @At("RETURN"), cancellable = true)
    private void hasEnoughImpulseToStartSprinting(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && this.hasEffect(ModRegistry.OVERENCUMBERED.get())) {
            cir.setReturnValue(false);
        }
    }
}
