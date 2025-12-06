package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

class LiquidBlockInteraction implements FaucetSource.Fluid {

    @Nullable
    @Override
    public FluidOffer getProvidedFluid(Level level, BlockPos pos, Direction dir, FluidState source) {
        if (source.isEmpty() || !source.isSource() || source.getType() instanceof FiniteFluid) return null;
        return FluidOffer.of(SoftFluidStack.fromFluid(source.getType(), SoftFluid.BUCKET_COUNT, level.registryAccess()),
                source.getType() != Fluids.WATER ? SoftFluid.BUCKET_COUNT : 1);
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, FluidState source, int amount) {
        BlockState state = level.getBlockState(pos);
        if (source.getType() != Fluids.WATER) {
            //bad
            if (state.getBlock() instanceof BucketPickup bp) {
                ItemStack bucket = bp.pickupBlock(null, level, pos, state);
            } else level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
