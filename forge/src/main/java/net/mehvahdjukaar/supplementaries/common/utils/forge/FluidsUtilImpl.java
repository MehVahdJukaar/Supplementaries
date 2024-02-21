package net.mehvahdjukaar.supplementaries.common.utils.forge;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidTankImpl;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

@SuppressWarnings("ConstantConditions")
public class FluidsUtilImpl {

    public static boolean tryExtractFromFluidHandler(BlockEntity tileBack, Block backBlock, Direction dir,
                                                     SoftFluidTank tempFluidHolder, FaucetBlockTile.FillAction fillAction) {
        IFluidHandler handlerBack = tileBack.getCapability(ForgeCapabilities.FLUID_HANDLER, dir).orElse(null);
        //TODO: fix create fluid int bug
        if (handlerBack != null && !Utils.getID(backBlock).getPath().equals("fluid_interface")) {
            //only works in 250 increment
            if (handlerBack.getFluidInTank(0).getAmount() < 250) return false;
            ((SoftFluidTankImpl) tempFluidHolder).copy(handlerBack);
            tempFluidHolder.getFluid().setCount(2);
            if (fillAction == null) return true;
            if (fillAction.tryExecute()) {
                handlerBack.drain(250, IFluidHandler.FluidAction.EXECUTE);
                tileBack.setChanged();
                return true;
            }
        }
        return false;
    }

    public static boolean tryFillFluidTank(BlockEntity tileBelow, SoftFluidTank tempFluidHolder) {
        boolean result;
        IFluidHandler handlerDown = tileBelow.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElse(null);
        if (handlerDown != null) {
            result = ((SoftFluidTankImpl) tempFluidHolder).transferToFluidTank(handlerDown, tempFluidHolder.getFluidCount() - 1);
            if (result) {
                tileBelow.setChanged();
                tempFluidHolder.getFluid().setCount(5);
                tempFluidHolder.capCapacity();
                return true;
            }
        }
        return false;
    }

    public static boolean hasFluidHandler(Level level, BlockPos pos, Direction dir) {
        return FluidUtil.getFluidHandler(level, pos, dir).isPresent();
    }

}
