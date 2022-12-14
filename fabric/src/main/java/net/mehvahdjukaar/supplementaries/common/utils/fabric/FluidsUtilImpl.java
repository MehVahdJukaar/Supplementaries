package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.BooleanSupplier;

public class FluidsUtilImpl {

    public static boolean tryFillFluidTank(BlockEntity tileBelow, SoftFluidTank tempFluidHolder) {
        return false;
    }

    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        return false;
    }

    public static boolean tryExtractFromFluidHandler(BlockEntity tileBack, Block backBlock, Direction dir, SoftFluidTank tempFluidHolder, FaucetBlockTile.FillAction transferFunction) {
        return false;
    }
}
