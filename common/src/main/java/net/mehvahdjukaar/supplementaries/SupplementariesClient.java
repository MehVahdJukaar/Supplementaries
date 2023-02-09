package net.mehvahdjukaar.supplementaries;

public class SupplementariesClient {

    private static float partialTicks = 0;

    public static void initClient() {

    }

    public static float getPartialTicks(){
        return partialTicks;
    }

    public static void onRenderTick(float ticks){
        partialTicks = ticks;
    }
    //TODO: move client setup here



}
