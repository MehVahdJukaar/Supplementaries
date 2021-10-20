package net.mehvahdjukaar.supplementaries.common.capabilities;

import net.mehvahdjukaar.supplementaries.api.IAntiqueTextProvider;
import net.mehvahdjukaar.supplementaries.api.ICatchableMob;
import net.mehvahdjukaar.supplementaries.api.IFlowerModelProvider;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;


public class SupplementariesCapabilities {

    @CapabilityInject(ICatchableMob.class)
    public static final Capability<ICatchableMob> CATCHABLE_MOB_CAP = null;
    @CapabilityInject(IAntiqueTextProvider.class)
    public static final Capability<IAntiqueTextProvider> ANTIQUE_TEXT_CAP = null;
    //NYI
    @CapabilityInject(IFlowerModelProvider.class)
    public static final Capability<IFlowerModelProvider> FLOWER_MODEL_PROVIDER_CAP = null;


}
