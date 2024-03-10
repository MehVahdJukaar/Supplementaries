package net.mehvahdjukaar.supplementaries.mixins;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.mehvahdjukaar.supplementaries.client.QuiverArrowSelectGui;
import net.mehvahdjukaar.supplementaries.client.cannon.CannonController;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow
    private double xpos;

    @Shadow private double ypos;

    @WrapWithCondition(method = "turnPlayer",
            at = @At(target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V", value = "INVOKE")
    )
    protected boolean onMoveMouse(LocalPlayer instance, double yRot, double xRot) {
        if (QuiverArrowSelectGui.isActive() && ClientConfigs.Items.QUIVER_MOUSE_MOVEMENT.get()) {
            QuiverArrowSelectGui.onPlayerRotated( yRot);
        }
        else if(CannonController.isActive()){
            CannonController.onPlayerRotated(yRot, xRot);
            return false;
        }
        return true;
    }
}
