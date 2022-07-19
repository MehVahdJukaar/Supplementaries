package net.mehvahdjukaar.supplementaries.common.utils.forge;

import net.mehvahdjukaar.moonlight.api.fluids.ISoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.function.Supplier;

@SuppressWarnings("ConstantConditions")
public class FluidsUtilImpl {

    public static boolean tryExtractFromFluidHandler(BlockEntity tileBack, Block backBlock, Direction dir,
                                                     ISoftFluidTank tempFluidHolder, boolean doTransfer, Supplier<Boolean> transferFunction) {
        IFluidHandler handlerBack = tileBack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, dir).orElse(null);
        //TODO: fix create fluid int bug
        if (handlerBack != null && !Utils.getID(backBlock).getPath().equals("fluid_interface")) {
            //only works in 250 increment
            if (handlerBack.getFluidInTank(0).getAmount() < 250) return false;
            ((SoftFluidTank) tempFluidHolder).copy(handlerBack);
            tempFluidHolder.setCount(2);
            if (doTransfer && transferFunction.get()) {
                handlerBack.drain(250, IFluidHandler.FluidAction.EXECUTE);
                tileBack.setChanged();
                return true;
            }
        }
        return false;
    }

    public static boolean tryFillFluidTank(BlockEntity tileBelow, ISoftFluidTank tempFluidHolder) {
        boolean result;
        IFluidHandler handlerDown = tileBelow.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, Direction.UP).orElse(null);
        if (handlerDown != null) {
            result = ((SoftFluidTank) tempFluidHolder).tryTransferToFluidTank(handlerDown, tempFluidHolder.getCount() - 1);
            if (result) {
                tileBelow.setChanged();
                tempFluidHolder.fillCount();
            }
            return true;
        }
        return false;
    }
}
