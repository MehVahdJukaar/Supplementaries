package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

class WaterBlockInteraction implements FaucetSource.Fluid {

    @Override
    public FluidOffer getProvidedFluid(Level level, BlockPos pos, Direction dir, FluidState source) {
        if (source.isEmpty() || !source.isSource()) return null;
        return FluidOffer.of(SoftFluidStack.fromFluid(source.getType(), SoftFluid.BUCKET_COUNT, null),
                SoftFluid.BUCKET_COUNT);
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, FluidState source, int amount) {
        if (source.getType() != Fluids.WATER) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
