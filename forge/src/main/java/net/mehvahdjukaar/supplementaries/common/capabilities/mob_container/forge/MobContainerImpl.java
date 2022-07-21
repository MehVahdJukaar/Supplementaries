package net.mehvahdjukaar.supplementaries.common.capabilities.mob_container.forge;

import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.common.capabilities.forge.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.common.capabilities.mob_container.DefaultCatchableMobCap;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.LazyOptional;

public class MobContainerImpl {
    public static <E extends Entity> ICatchableMob getCap(E entity) {
        if (entity == null) return null;
        ICatchableMob cap;
        if (entity instanceof ICatchableMob) cap = (ICatchableMob) entity;
        else {
            LazyOptional<ICatchableMob> opt = entity.getCapability(CapabilityHandler.CATCHABLE_MOB_CAP);
            cap = opt.orElseGet(() -> DefaultCatchableMobCap.getDefaultCap(entity));
        }
        return cap;
    }
}
