package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.candlelight.api.PlatformImpl;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;

public class QuarkClientCompat {

    @PlatformImpl
    public static void initClient() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void registerEntityRenderers(ClientHelper.BlockEntityRendererEvent event) {
        throw new AssertionError();
    }

    @PlatformImpl
    public static void setupClient() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean shouldHaveSuppButtonOnRight() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean canRenderBlackboardTooltip() {
        throw new AssertionError();
    }

    @PlatformImpl
    public static boolean canRenderQuarkTooltip() {
        throw new AssertionError();
    }


}
