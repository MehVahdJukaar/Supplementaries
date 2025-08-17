package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.supplementaries.common.block.blocks.LunchBoxBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.LunchBoxBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LunchBasketColor implements BlockColor {
    @Override
    public int getColor(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int i) {
        LunchBoxBlockTile be = ModRegistry.LUNCH_BASKET_TILE.get().getBlockEntity(blockAndTintGetter, blockPos);
        if (be != null) {
             DyedItemColor dc = be.getDyedColor();
            if(dc != null){
                return dc.rgb();
            }
        }
        return 0;
    }
}
