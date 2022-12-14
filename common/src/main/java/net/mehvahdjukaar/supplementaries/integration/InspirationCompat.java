package net.mehvahdjukaar.supplementaries.integration;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class InspirationCompat {
    public static InteractionResult doCauldronStuff(BlockEntity blockEntity, SoftFluidTank tempFluidHolder,
                                                    FaucetBlockTile.FillAction o) {
        return InteractionResult.PASS;
    }

    public static InteractionResult tryAddFluid(BlockEntity blockEntity, SoftFluidTank tempFluidHolder) {
        return InteractionResult.PASS;
    }
}
