package net.mehvahdjukaar.supplementaries.mixins.neoforge;

import net.mehvahdjukaar.supplementaries.client.renderers.neoforge.PicklePlayer;
import net.minecraft.client.gui.screens.ChatScreen;
import net.neoforged.neoforge.common.loot.LootModifierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatMixin {

    @Inject(method = "handleChatInput", at = @At("HEAD"))
    private void interceptMessage(String message, boolean addToRecentChat, CallbackInfo ci) {
        PicklePlayer.onChatEvent(message);
     }

}