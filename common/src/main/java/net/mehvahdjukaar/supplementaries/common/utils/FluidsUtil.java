package net.mehvahdjukaar.supplementaries.common.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FluidOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class FluidsUtil {

    @Contract
    @ExpectPlatform
    public static boolean extractFluidFromTank(BlockEntity tileBack, Direction dir, int amount) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Integer fillFluidTank(BlockEntity tileBelow, FluidOffer offer, Direction dir) {
        throw new AssertionError();
    }

    @Contract
    @ExpectPlatform
    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        throw new AssertionError();
    }

    @Nullable
    @Contract
    @ExpectPlatform
    public static FluidOffer getFluidInTank(Level level, BlockPos pos, Direction dir, BlockEntity source) {
        throw new AssertionError();
    }
}
