package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.moonlight.api.fluids.ISoftFluidTank;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

public class InspirationCompat {
    public static boolean doCauldronStuff(BlockEntity blockEntity, ISoftFluidTank tempFluidHolder, boolean doTransfer,
                                          Supplier<Boolean> o) {
        return false;
    }

    public static boolean tryAddFluid(BlockEntity blockEntity, ISoftFluidTank tempFluidHolder) {
        return false;
    }
}
