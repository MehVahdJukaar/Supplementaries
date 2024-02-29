package net.mehvahdjukaar.supplementaries.common.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Contract;

public class FluidsUtil {

    @Contract
    @ExpectPlatform
    public static boolean extractFluidFromTank(BlockEntity tileBack, Direction dir, int amount) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Integer fillFluidTank(BlockEntity tileBelow, SoftFluidStack tempFluidHolder) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static SoftFluidStack getFluidInTank(Level level, BlockPos pos, Direction dir, BlockEntity source) {
        throw new AssertionError();
    }
}
