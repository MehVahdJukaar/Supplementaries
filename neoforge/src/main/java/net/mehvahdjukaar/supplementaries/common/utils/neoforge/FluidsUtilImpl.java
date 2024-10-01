package net.mehvahdjukaar.supplementaries.common.utils.neoforge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.neoforge.SoftFluidStackImpl;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FluidOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@SuppressWarnings("ConstantConditions")
public class FluidsUtilImpl {

    public static boolean extractFluidFromTank(BlockEntity tileBack, Direction dir, int amount) {
        IFluidHandler handlerBack = tileBack.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                tileBack.getBlockPos(), tileBack.getBlockState(), tileBack, dir);
        if (handlerBack != null) {
            //only works in 250 increment
            if (handlerBack.drain(250 * amount, IFluidHandler.FluidAction.SIMULATE).getAmount() != 250 * amount)
                return false;
            handlerBack.drain(250 * amount, IFluidHandler.FluidAction.EXECUTE);
            tileBack.setChanged();
            return true;
        }
        return false;
    }

    public static Integer fillFluidTank(BlockEntity tileBelow, FluidOffer offer, Direction dir) {
        IFluidHandler handlerDown = tileBelow.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                tileBelow.getBlockPos(), tileBelow.getBlockState(), tileBelow, dir);
        if (handlerDown != null && offer.fluid() instanceof SoftFluidStackImpl impl) {
            FluidStack stack = impl.toForgeFluid();
            if (!stack.isEmpty()) {
                stack.setAmount(250 * offer.minAmount());
                if (stack.isEmpty()) return null;
                int filled = handlerDown.fill(stack, IFluidHandler.FluidAction.EXECUTE);
                tileBelow.setChanged();

                return Mth.ceil(filled / 250f);
            }
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
            FluidStack fluidInTank = opt.get().drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if (!fluidInTank.isEmpty()) {
                if (!Utils.getID(source.getBlockState().getBlock()).getPath().equals("fluid_interface")) {
                    return SoftFluidStackImpl.fromForgeFluid(fluidInTank);
                }
            }
        }
        return SoftFluidStack.empty();
    }

}
