package net.mehvahdjukaar.supplementaries.common.utils.forge;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.utils.VibeChecker;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

public class VibeCheckerImpl {
    public static void checkVibe() {
        crashIfOptifineHasNukedForge();

        if(PlatHelper.isModLoaded("immediatelyfast")){
            Supplementaries.LOGGER.warn("Immediately fast was detected. Colored maps and map texture mipmap will not work unless you turn map changes off in IF configs");
        }

    }

    //will crash anyways. we just end the suffering earlier
    private static void crashIfOptifineHasNukedForge() {
        if (PlatHelper.isModLoaded("optifinefixer")) return;
        try {
            new BakedQuad(new int[]{}, 0, Direction.UP, null, true, false);
        } catch (Exception e) {
            if (e instanceof NoSuchMethodException) {
                throw new VibeChecker.BadModError("Your Optifine version is incompatible with Forge. Refusing to continue further", e);
            }
        }
    }

}
