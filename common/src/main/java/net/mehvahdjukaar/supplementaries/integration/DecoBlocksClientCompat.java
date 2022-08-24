package net.mehvahdjukaar.supplementaries.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class DecoBlocksClientCompat {

    @ExpectPlatform
    public static void registerRenderLayers(){
        throw  new AssertionError();
    }
}
