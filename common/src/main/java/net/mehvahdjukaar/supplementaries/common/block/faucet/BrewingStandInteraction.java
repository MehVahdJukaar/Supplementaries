package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.blocks.FrameBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.FaucetBlockTile;
import net.mehvahdjukaar.supplementaries.mixins.BrewingStandMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
                if (faucetTank.tryDrainItem(stack, level, pos, true, false) != null) {
                    ItemStack empty = faucetTank.tryDrainItem(stack, level, pos, false, false);
                    faucetTank.setCount(2);
                    if (fillAction == null) return InteractionResult.CONSUME;
                    if (fillAction.tryExecute()) {
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
                var result = faucetTank.tryFillingItem(stack.getItem(), level, pos, false, false);
                if (result != null) {
                    brewingStand.setItem(i, result.copy());
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

