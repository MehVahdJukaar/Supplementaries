package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;
import net.mehvahdjukaar.moonlight.api.misc.HolderReference;
import net.mehvahdjukaar.moonlight.api.misc.HolderReference;
import net.mehvahdjukaar.supplementaries.Supplementaries;

public class ModSoftFluids {

    public static final HolderReference<SoftFluid> DIRT = HolderReference.of(Supplementaries.res("dirt"), SoftFluidRegistry.KEY);
    public static final HolderReference<SoftFluid> SAP = HolderReference.of(Supplementaries.res("sap"), SoftFluidRegistry.KEY);


}
