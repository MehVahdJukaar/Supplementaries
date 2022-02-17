package net.mehvahdjukaar.supplementaries.mixins;
import net.mehvahdjukaar.supplementaries.client.gui.PresentBlockGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerSocialManager.class)
public abstract class PlayerSocialManagerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "addPlayer", at = @At("TAIL"))
    public void addPlayer(PlayerInfo info, CallbackInfo ci) {
        Screen screen = this.minecraft.screen;
        if (screen instanceof PresentBlockGui gui) {
            gui.onAddPlayer(info);
        }
    }

    @Inject(method = "removePlayer", at = @At("TAIL"))
    public void removePlayer(UUID id, CallbackInfo ci) {
        Screen screen = this.minecraft.screen;
        if (screen instanceof PresentBlockGui gui) {
            gui.onRemovePlayer(id);
        }
    }
}
