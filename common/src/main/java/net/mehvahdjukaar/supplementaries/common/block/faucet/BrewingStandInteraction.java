package net.mehvahdjukaar.supplementaries.common.block.faucet;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

//consume to finish current group
class BrewingStandInteraction implements FaucetSource.Tile, FaucetTarget.Tile {

    @Override
    public FluidOffer getProvidedFluid(Level level, BlockPos pos, Direction dir, BlockEntity tile) {
        if (tile instanceof BrewingStandBlockEntity brewingStand) {
            for (int i = 0; i < 3; i++) {
                ItemStack stack = brewingStand.getItem(i);
                //simulate draining
                var opt = SoftFluidStack.fromItem(stack, level.registryAccess());
                if (opt != null) {
                    return FluidOffer.of(opt.getFirst());
                }
            }
        }
        return null;
    }

    @Override
    public void drain(Level level, BlockPos pos, Direction dir, BlockEntity tile, int amount) {
        if (tile instanceof BrewingStandBlockEntity brewingStand) {
            for (int i = 0; i < 3; i++) {
                ItemStack stack = brewingStand.getItem(i);
                //simulate draining
                var opt = SoftFluidStack.fromItem(stack, level.registryAccess());
                if (opt != null) {
                    ItemStack remainder = (stack.getItem().hasCraftingRemainingItem() ?
                            Items.GLASS_BOTTLE.getDefaultInstance() :
                            ForgeHelper.getCraftingRemainingItem(stack).get());
                    brewingStand.setItem(i, remainder);
                    tile.setChanged();
                    return;
                }
            }
        }
    }

    @Override
    public Integer fill(Level level, BlockPos pos, BlockEntity tile, FluidOffer offer) {
        if (tile instanceof BrewingStandBlockEntity brewingStand) {
            int needToPlace = offer.minAmount();
            ItemStack[] toPlace = new ItemStack[3];
            for (int i = 0; i < 3; i++) {
                if (needToPlace <= 0) break;
                ItemStack stack = brewingStand.getItem(i);
                var filled = offer.fluid().toItem(stack);
                if (filled != null) {
                    ItemStack filledItem = filled.getFirst();
                    brewingStand.setItem(i, ItemStack.EMPTY);
                    if (brewingStand.canPlaceItem(i, filledItem)) {
                        toPlace[i] = filledItem;
                        needToPlace--;
                    }
                    brewingStand.setItem(i, stack);
                }
            }
            if (needToPlace == 0) {
                for (int i = 0; i < 3; i++) {
                    if (toPlace[i] != null) {
                        brewingStand.setItem(i, toPlace[i]);
                    }
                }
                tile.setChanged();
                return offer.minAmount();
            }
        }
        return null;
    }
}

