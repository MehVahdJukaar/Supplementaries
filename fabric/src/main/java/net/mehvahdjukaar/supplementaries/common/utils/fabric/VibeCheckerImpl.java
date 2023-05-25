package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;

public class VibeCheckerImpl {
    public static void checkVibe() {
        crashIfFabricRenderingAPIHasBeenNuked();
        crashWhenStolenMod();
    }

    //Its been prooven that CFTS is primarely made up of code shamelessly stolen from frozen up
    //enfoncing its ARR license
    private static void crashWhenStolenMod() {
        String s = "creaturesfromthesnow";
        if (PlatHelper.isModLoaded(s)) {
            throw new IllegalStateException("The mod "+s+" contains stolen assets and code from Frozen Up which is ARR. Enforcing its license by refusing to continue further");
        }
    }

    //I hate this. I've got to do what I've got to do. Cant stand random reports anymore
    //you were supposed to destroy the loader api nuking mods, not join them!
    public static void crashIfFabricRenderingAPIHasBeenNuked() {
        if (PlatHelper.isModLoaded("sodium") && !PlatHelper.isModLoaded("indium")) {
            throw new IllegalStateException("You seem to have installed Sodium which breaks fabric rendering API." +
                    "To fix you must install Indium as Supplementaries, as many other mods, rely on said API");
        }
    }
}
