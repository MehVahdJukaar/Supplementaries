package net.mehvahdjukaar.supplementaries.common.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Contract;

import java.util.function.BooleanSupplier;

public class FluidsUtil {

    @ExpectPlatform
    public static boolean tryExtractFromFluidHandler(BlockEntity tileBack, Block backBlock, Direction dir,
                                                     SoftFluidTank tempFluidHolder, boolean doTransfer, BooleanSupplier transferFunction) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean tryFillFluidTank(BlockEntity tileBelow, SoftFluidTank tempFluidHolder) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        throw new AssertionError();
    }
}
