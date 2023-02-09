package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import org.jetbrains.annotations.Contract;

public class QuarkClientCompat {

    @Contract
    @ExpectPlatform
    public static boolean shouldHaveButtonOnRight() {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean canRenderBlackboardTooltip() {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean canRenderQuarkTooltip() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerTooltipComponent(ClientPlatformHelper.TooltipComponentEvent event) {
        throw new AssertionError();
    }

}
