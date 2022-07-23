package net.mehvahdjukaar.supplementaries.reg;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;
import net.mehvahdjukaar.supplementaries.Supplementaries;

public class ModSoftFluids {

    public static final DataObjectReference<SoftFluid> DIRT = new DataObjectReference<>(Supplementaries.res("dirt"), SoftFluidRegistry.getRegistryKey());
    public static final DataObjectReference<SoftFluid> SAP = new DataObjectReference<>(Supplementaries.res("sap"), SoftFluidRegistry.getRegistryKey());


}
