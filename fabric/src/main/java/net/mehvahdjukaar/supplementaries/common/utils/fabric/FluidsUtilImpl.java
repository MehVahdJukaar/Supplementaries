package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.ISoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

public class FluidsUtilImpl {
    public static boolean tryExtractFromFluidHandler(BlockEntity tileBack, Block backBlock, Direction dir,
                                                     ISoftFluidTank tempFluidHolder, boolean doTransfer, Supplier<Boolean> transferFunction) {
        return false;
    }

    public static boolean tryFillFluidTank(BlockEntity tileBelow, ISoftFluidTank tempFluidHolder) {
        return false;
    }

    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        return false;
    }
}
