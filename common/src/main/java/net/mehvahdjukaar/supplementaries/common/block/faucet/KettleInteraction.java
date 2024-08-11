package net.mehvahdjukaar.supplementaries.common.block.faucet;
/*
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.supplementaries.integration.FarmersRespriteCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

class KettleInteraction implements FaucetTarget.BlState, FaucetSource.BlState {

    @Override
    public FluidOffer getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockState state) {
        if (FarmersRespriteCompat.isKettle(state)) {
            var p = FarmersRespriteCompat.getWaterLevel();
            int waterLevel = state.getValue(p);
            if (waterLevel > 0) {
                return FluidOffer.of(BuiltInSoftFluids.WATER.getHolder(), waterLevel);
            }
        }
        return null;
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockState state, int amount) {
        if (FarmersRespriteCompat.isKettle(state)) {
            var p = FarmersRespriteCompat.getWaterLevel();
            int waterLevel = state.getValue(p);
            amount = Math.min(amount, waterLevel);
            level.setBlock(pos, state.setValue(p, amount), 3);
        }
    }

    @Override
    public Integer fill(Level level, BlockPos pos, BlockState state, SoftFluidStack fluid, int minAmount) {
        if (FarmersRespriteCompat.isKettle(state)) {
            var p = FarmersRespriteCompat.getWaterLevel();
            int waterLevel = state.getValue(p);
            if (waterLevel == 3) return 0; //exit early
            int newWater = Math.max(waterLevel + minAmount, 3);
            level.setBlock(pos, state.setValue(p, newWater), 3);
            return minAmount;
        }
        return null;
    }
}
*/