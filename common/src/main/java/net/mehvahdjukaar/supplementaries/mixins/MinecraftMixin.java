package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 0))
    private boolean supp$cancelF5WhenControllingCannon(boolean original) {
        if (CannonController.isActive()) return false;
        return original;
    }
}
