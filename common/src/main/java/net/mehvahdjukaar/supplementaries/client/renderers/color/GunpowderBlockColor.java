package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.supplementaries.common.block.blocks.GunpowderBlock;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class GunpowderBlockColor implements BlockColor {

    private static final int[] COLORS = new int[9];

    static {
        for (int i = 0; i < 9; i++) {
            float litAmount =  i / 8.0F;
            float red = litAmount * 0.7F + 0.3F;

            float green = litAmount * litAmount * 0.4F + 0.3F;
            float blue = 0.3F;

            if (green < 0.0F) {
                green = 0.0F;
            }

            if (blue < 0.0F) {
                blue = 0.0F;
            }

            int redInt = Mth.clamp(Mth.floor(red * 255), 0, 255);
            int greenInt = Mth.clamp(Mth.floor(green * 255), 0, 255);
            int blueInt = Mth.clamp(Mth.floor(blue * 255), 0, 255);
            COLORS[i] = FastColor.ARGB32.color(0, redInt, greenInt, blueInt);;
            //if(i==0) COLORS[i] = 0xffffff;
            // return 6579300;
        }
    }

    @Override
    public int getColor(BlockState state, BlockAndTintGetter reader, BlockPos pos, int color) {
        return COLORS[state.getValue(GunpowderBlock.BURNING)];
    }
}