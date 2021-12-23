package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.supplementaries.common.block.util.IBlockHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FlowerBoxColor implements BlockColor {

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
        if (world != null && pos != null) {
            if (world.getBlockEntity(pos) instanceof IBlockHolder bh) {
                if (tint < 3 && tint >= 0) {
                    BlockState mimic = bh.getHeldBlock(tint);
                    if (mimic != null) {
                        return Minecraft.getInstance().getBlockColors().getColor(mimic, world, pos, tint);
                    }
                }
            }
        }
        return -1;
    }
}

