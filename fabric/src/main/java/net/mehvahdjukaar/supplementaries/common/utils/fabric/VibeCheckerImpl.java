package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;

public class VibeCheckerImpl {
    public static void checkVibe() {
        crashIfFabricRenderingAPIHasBeenNuked();
    }



    //I hate this. I've got to do what I've got to do. Cant stand random reports anymore
    //you were supposed to destroy the loader api nuking mods, not join them!
    public static void crashIfFabricRenderingAPIHasBeenNuked() {
        if (PlatHelper.isModLoaded("sodium") && !PlatHelper.isModLoaded("indium")) {
            throw new VibeChecker.UnsupportedModError("You seem to have installed Sodium which breaks fabric rendering API." +
                    "To fix you must install Indium as Supplementaries, as many other mods, rely on said API");
        }
    }

}
