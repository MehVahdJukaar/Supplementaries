package net.mehvahdjukaar.supplementaries.common.capabilities.mob_container.forge;

import net.mehvahdjukaar.supplementaries.common.capabilities.forge.CapabilityHandler;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Optional;

public class CapturedMobHandlerImpl {

    @Nullable
    public static ICatchableMob getForgeCap(Entity entity) {
        Optional<ICatchableMob> opt = entity.getCapability(CapabilityHandler.CATCHABLE_MOB_CAP).resolve();
        return opt.orElse(null);
    }
}
