package net.mehvahdjukaar.supplementaries.common.utils.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidStackImpl;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FluidOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("ConstantConditions")
public class FluidsUtilImpl {

    public static boolean extractFluidFromTank(BlockEntity tileBack, Direction dir, int amount) {
        IFluidHandler handlerBack = tileBack.getCapability(ForgeCapabilities.FLUID_HANDLER, dir).orElse(null);
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

    public static Integer fillFluidTank(BlockEntity tileBelow, FluidOffer offer) {
        IFluidHandler handlerDown = tileBelow.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElse(null);
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
            for (int i = 1; i <= 4; i++){
                int toDrain = i * 250;
                FluidStack fluidInTank = opt.resolve().get().drain(toDrain, IFluidHandler.FluidAction.SIMULATE);
                if (!fluidInTank.isEmpty()) {
                    SoftFluidStack forgeFluid = SoftFluidStackImpl.fromForgeFluid(fluidInTank);
                    if (!forgeFluid.isEmpty()) {
                        int actualAmount = fluidInTank.getAmount() / 250;
                        //TODO: technically here we could try all lower amounts too to find the min but its probably not worth it
                        return FluidOffer.of(forgeFluid, actualAmount);
                    }
                }
            }

        }
        return null;
    }

}
