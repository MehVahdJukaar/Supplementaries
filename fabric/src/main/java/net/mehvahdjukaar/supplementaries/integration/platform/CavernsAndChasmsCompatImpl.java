package net.mehvahdjukaar.supplementaries.integration.platform;

import net.minecraft.world.entity.Entity;

public class CavernsAndChasmsCompatImpl {

    // Caverns & Chasms is not available on Fabric
    public static boolean maybeCleanDirtyRat(Entity entity) {
        return false;
    }
}
