package net.mehvahdjukaar.supplementaries.fabric;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;

public class VibeChecker {

    public static void checkVibe(){
        crashIfFabricRenderingAPIHasBeenNuked();
        crashWhenStolenMod();
    }

    //I hate this. I've got to do what I've got to do. Cant stand random reports anymore and mods wont work like this anyways
    private static void crashIfFabricRenderingAPIHasBeenNuked() {
        if (PlatformHelper.isModLoaded("sodium") && !PlatformHelper.isModLoaded("indium")) {
            throw new UnsupportedModException("You seem to have installed Sodium which breaks fabric rendering API." +
                    "To fix you must install Indium as Supplementaries, as many other mods, rely on said API");
        }
    }

    //Its been prooven that CFTS is primarely made up of code shamelessly stolen from frozen up
    //enfoncing its ARR license
    private static void crashWhenStolenMod() {
        String s = "creaturesfromthesnow";
        if (PlatformHelper.isModLoaded(s)) {
            throw new UnsupportedModException("The mod "+s+" contains stolen assets and code from Frozen Up which is ARR. Enforcing its license by refusing to continue further");
        }
    }

    private static class UnsupportedModException extends RuntimeException{

        public UnsupportedModException(String s) {
            super(s);
        }
    }
}
