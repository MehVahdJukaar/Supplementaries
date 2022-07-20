package net.mehvahdjukaar.supplementaries.common.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.ISoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

public class FluidsUtil {

    @ExpectPlatform
    public static boolean tryExtractFromFluidHandler(BlockEntity tileBack, Block backBlock, Direction dir,
                                                     ISoftFluidTank tempFluidHolder, boolean doTransfer, Supplier<Boolean> transferFunction) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean tryFillFluidTank(BlockEntity tileBelow, ISoftFluidTank tempFluidHolder) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        throw new AssertionError();
    }
}
