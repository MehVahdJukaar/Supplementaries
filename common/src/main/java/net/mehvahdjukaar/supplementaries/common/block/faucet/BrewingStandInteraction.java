package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.Nullable;

//consume to finish current group
class BrewingStandInteraction implements
        IFaucetTileSource, IFaucetTileTarget {

    @Override
    public InteractionResult tryDrain(Level level, SoftFluidTank faucetTank,
                                      BlockPos pos, BlockEntity tile, Direction dir,
                                      @Nullable FaucetBlockTile.FillAction fillAction) {
        if (tile instanceof BrewingStandBlockEntity brewingStand) {
            for (int i = 0; i < 3; i++) {
                ItemStack stack = brewingStand.getItem(i);
                //simulate draining
                InteractionResultHolder<ItemStack> result = faucetTank.drainItem(stack, level, pos, true, false);
                if(result.getResult().consumesAction()) {
                    ItemStack empty = result.getObject();
                    faucetTank.getFluid().setCount(2); //replenish
                    if (fillAction == null) return InteractionResult.CONSUME;
                    if (fillAction.tryExecute()) {
                        //actually empties
                        faucetTank.drainItem(stack, level, pos, false, false);
                        brewingStand.setItem(i, empty.copy());//should never be null since we simulated
                        tile.setChanged();
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult tryFill(Level level, SoftFluidTank faucetTank, BlockPos pos, BlockEntity tile) {
        if (tile instanceof BrewingStandBlockEntity brewingStand) {
            for (int i = 0; i < 3; i++) {
                ItemStack stack = brewingStand.getItem(i);
                InteractionResultHolder<ItemStack> result = faucetTank.fillItem(stack, level, pos, false, false);
                if (result.getResult().consumesAction()) {
                    brewingStand.setItem(i, result.getObject().copy());
                    tile.setChanged();
                    //TODO: fix visual update
                    //BlockState s = tile.getBlockState();
                    //level.sendBlockUpdated(tile.getBlockPos(), s, s.cycle(BrewingStandBlock.HAS_BOTTLE[0]), 3);
                    //level.sendBlockUpdated(tile.getBlockPos(), s, s.cycle(BrewingStandBlock.HAS_BOTTLE[0]), 2);

                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}

