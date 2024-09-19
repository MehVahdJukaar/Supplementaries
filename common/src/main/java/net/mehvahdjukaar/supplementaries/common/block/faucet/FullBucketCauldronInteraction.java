package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

record FullBucketCauldronInteraction(BlockState fullCauldron,
                                     ItemStack filleBucket) implements FaucetTarget.BlState, FaucetSource.BlState {


    @Override
    public FluidOffer getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockState source) {
        if (source.is(fullCauldron.getBlock())) {
            var pair = SoftFluidStack.fromItem(filleBucket.copy());
            if (pair != null) {
                var stack = pair.getFirst();
                int amount;
                if (source.hasProperty(LayeredCauldronBlock.LEVEL)) {
                    amount = source.getValue(LayeredCauldronBlock.LEVEL);
                } else {
                    amount = stack.getCount();
                }
                return FluidOffer.of(stack.getHolder(), amount, amount);
            }
        }
        return null;
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockState source, int amount) {
        level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
    }

    @Override
    public Integer fill(Level level, BlockPos pos, BlockState state, FluidOffer offer) {
        if (state.is(Blocks.CAULDRON) || (state.is(fullCauldron.getBlock()) && state.hasProperty(LayeredCauldronBlock.LEVEL))) {
            var pair = SoftFluidStack.fromItem(filleBucket.copy());

            if (pair != null) {
                SoftFluidStack fluidStack = pair.getFirst();
                int minAmount = offer.minAmount();
                SoftFluidStack wantToInsert = offer.fluid();
                if (wantToInsert.is(fluidStack.getHolder())) {
                    BlockState toPlace = fullCauldron;
                    int added;
                    if (state.hasProperty(LayeredCauldronBlock.LEVEL)) {
                        int current = state.getValue(LayeredCauldronBlock.LEVEL);
                        if (current == 3) return null; //already full
                        int newAmount = current + minAmount;
                        if (newAmount > 3) {
                            return null;
                        }
                        added = minAmount;
                        toPlace = toPlace.setValue(LayeredCauldronBlock.LEVEL, newAmount);
                    } else {
                        if (wantToInsert.getCount() < fluidStack.getCount() && minAmount < fluidStack.getCount()) {
                            return null;
                        }
                        added = fluidStack.getCount();
                    }
                    level.setBlock(pos, toPlace, 3);

                    return added;
                }
            }
        }
        return null;
    }
}

