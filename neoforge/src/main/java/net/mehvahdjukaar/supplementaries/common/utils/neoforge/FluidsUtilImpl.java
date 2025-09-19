package net.mehvahdjukaar.supplementaries.common.utils.neoforge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.neoforge.SoftFluidStackImpl;
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
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public static FluidOffer getFluidInTank(Level level, BlockPos pos, Direction dir, BlockEntity source) {
        var opt = FluidUtil.getFluidHandler(level, pos, dir);
        if (opt.isPresent()) {
            //simulate all possible amounts
            for (int i = 4; i > 0; i--) {
                int toDrain = i * 250;
                FluidStack fluidInTank = opt.get().drain(toDrain, IFluidHandler.FluidAction.SIMULATE);
                if (!fluidInTank.isEmpty()) {
                    SoftFluidStack forgeFluid = SoftFluidStackImpl.fromForgeFluid(fluidInTank);
                    if (!forgeFluid.isEmpty()) {
                        //TODO: technically here we could try all lower amounts too to find the min but its probably not worth it
                        return FluidOffer.of(forgeFluid.getHolder(), i, i);
                    }
                }
            }

        }
        return null;
    }

}
