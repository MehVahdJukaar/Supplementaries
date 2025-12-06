package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.HolderRef;
import net.mehvahdjukaar.moonlight.api.misc.HolderReference;
import net.mehvahdjukaar.supplementaries.Supplementaries;

public class ModSoftFluids {

    public static final HolderRef<SoftFluid> DIRT = HolderRef.of(Supplementaries.res("dirt"), SoftFluidRegistry.KEY);
    public static final HolderRef<SoftFluid> SAP = HolderRef.of(Supplementaries.res("sap"), SoftFluidRegistry.KEY);


}
