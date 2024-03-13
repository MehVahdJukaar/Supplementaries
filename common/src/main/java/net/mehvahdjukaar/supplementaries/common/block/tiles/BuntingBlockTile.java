package net.mehvahdjukaar.supplementaries.common.block.tiles;


import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BuntingBlock;
import net.mehvahdjukaar.supplementaries.common.items.BuntingItem;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Map;


public class BuntingBlockTile extends ItemDisplayTile { //implements IExtraModelDataProvider

    // client model cache
    private final Map<Direction, DyeColor> buntings = new EnumMap<>(Direction.class);

    public BuntingBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.BUNTING_TILE.get(), pos, state, 4);
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Bunting");
    }

    @Override
    public void updateClientVisualsOnLoad() {
        super.updateClientVisualsOnLoad();
        buntings.clear();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            ItemStack stack = this.getItem(d.get2DDataValue());
            if (stack.getItem() instanceof BuntingItem) {
                DyeColor color = BuntingItem.getColor(stack);
                if (color != null) {
                    this.buntings.put(d, color);
                }
            }
        }
    }

    @Override
    public void updateTileOnInventoryChanged() {
        if (this.isEmpty()) {
            level.setBlockAndUpdate(worldPosition, ModRegistry.ROPE.get()
                    .withPropertiesOf(getBlockState()));
        }
    }

    public Map<Direction, DyeColor> getBuntings() {
        return buntings;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return stack.getItem() instanceof BuntingItem && getItem(index).isEmpty() &&
                BuntingBlock.canSupportBunting(getBlockState(), index);
    }


}