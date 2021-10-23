package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.supplementaries.block.blocks.CogBlock;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class CogBlockColor implements BlockColor {

    private static final int[] COLORS = new int[16];

    static {
        for (int i = 0; i <= 15; ++i) {
            float f = (float) i / 15.0F;
            float f1 = f * 0.5F + (f > 0.0F ? 0.5F : 0.3F);
            float f2 = Mth.clamp(f * f * 0.5F - 0.3F, 0.0F, 1.0F);
            float f3 = Mth.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
            COLORS[i] = Mth.color(f1, f2, f3);
        }
    }

    @Override
    public int getColor(BlockState state, BlockAndTintGetter reader, BlockPos pos, int color) {
        return COLORS[state.getValue(CogBlock.POWER)];
    }
}