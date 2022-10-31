package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class InspirationCompat {
    public static boolean doCauldronStuff(BlockEntity blockEntity, SoftFluidTank tempFluidHolder, boolean doTransfer,
                                          BooleanSupplier o) {
        return false;
    }

    public static boolean tryAddFluid(BlockEntity blockEntity, SoftFluidTank tempFluidHolder) {
        return false;
    }
}
