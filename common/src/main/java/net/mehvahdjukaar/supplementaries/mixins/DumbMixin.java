package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.minecraft.client.renderer.Sheets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sheets.class)
public class DumbMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void dumbMixin(CallbackInfo ci) {
        //get current stack trace
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        VibeChecker.setSusStackTrace(stackTraceElements);
    }
}
