package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.supplementaries.common.block.util.IBlockHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MimicBlockColor implements BlockColor {

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
        return col(state, world, pos, tint);
    }

    public static int col(BlockState state, BlockAndTintGetter level, BlockPos pos, int tint){
        if (level != null && pos != null) {
            if (level.getBlockEntity(pos) instanceof IBlockHolder tile) {
                BlockState mimic = tile.getHeldBlock();
                if (mimic != null && !mimic.hasBlockEntity()) {
                    return Minecraft.getInstance().getBlockColors().getColor(mimic, level, pos, tint);
                }
            }
        }
        return -1;
    }

    public static class noParticle implements BlockColor{
        @Override
        public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
            if(tint  == 0) return -1;
            return col(state, world, pos, tint);
        }
    }
}

