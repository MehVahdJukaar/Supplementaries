package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FluidsUtilImpl {

    public static boolean extractFluidFromTank(BlockEntity tileBack, Direction dir, int amount) {
        return false;
    }

    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        return false;
    }

    public static Integer fillFluidTank(BlockEntity tileBelow, SoftFluidStack fluid) {
        return null;
    }

    @org.jetbrains.annotations.Contract
    public static SoftFluidStack getFluidInTank(Level level, BlockPos pos, Direction dir, BlockEntity source) {
        return SoftFluidStack.empty();
    }
}
