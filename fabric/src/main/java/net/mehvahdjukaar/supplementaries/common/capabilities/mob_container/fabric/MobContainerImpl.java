package net.mehvahdjukaar.supplementaries.common.capabilities.mob_container.fabric;

import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.capabilities.mob_container.DefaultCatchableMobCap;
import net.minecraft.world.entity.Entity;

public class MobContainerImpl {
    public static <E extends Entity> ICatchableMob getCap(E entity) {
        if (entity instanceof ICatchableMob catchableMob) return catchableMob;
        return null;
    }
}
