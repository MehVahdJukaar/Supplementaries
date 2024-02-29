package net.mehvahdjukaar.supplementaries.common.utils.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidTankImpl;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

@SuppressWarnings("ConstantConditions")
public class FluidsUtilImpl {

    public static boolean extractFluidFromTank(BlockEntity tileBack, Direction dir, int amount) {
        IFluidHandler handlerBack = tileBack.getCapability(ForgeCapabilities.FLUID_HANDLER, dir).orElse(null);
        //TODO: fix create fluid int bug
        if (handlerBack != null) {
            //only works in 250 increment
            if (handlerBack.getFluidInTank(0).getAmount() < 250) return false;
            handlerBack.drain(250 * amount, IFluidHandler.FluidAction.EXECUTE);
            tileBack.setChanged();
            return true;
        }
        return false;
    }

    public static Integer fillFluidTank(BlockEntity tileBelow, SoftFluidStack fluid) {
        IFluidHandler handlerDown = tileBelow.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElse(null);
        if (handlerDown != null) {
            var f = fluid.getVanillaFluid();
            FluidStack stack = new FluidStack(f, fluid.getCount() * 250, fluid.getTag());
            if (stack.isEmpty()) return null;
            var filled = handlerDown.fill(stack, IFluidHandler.FluidAction.EXECUTE);
            tileBelow.setChanged();

            return filled;
        }
        return null;
    }

    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        return FluidUtil.getFluidHandler(level, pos, dir).isPresent();
    }

    @org.jetbrains.annotations.Contract
    public static SoftFluidStack getFluidInTank(Level level, BlockPos pos, Direction dir, BlockEntity source) {
        var opt = FluidUtil.getFluidHandler(level, pos, dir);
        if (opt.isPresent()) {
            FluidStack fluidInTank = opt.resolve().get().getFluidInTank(0);
            if (!fluidInTank.isEmpty()) {
                if (!Utils.getID(source.getBlockState().getBlock()).getPath().equals("fluid_interface")) {
                    return SoftFluidTankImpl.convertForgeFluid(fluidInTank);
                }
            }
        }
        return SoftFluidStack.empty();
    }

}
