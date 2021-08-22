package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.supplementaries.block.blocks.GunpowderBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockDisplayReader;

public class GunpowderBlockColor implements  IBlockColor {

    private static final int[] COLORS = new int[9];

    static {
        for(int i = 0; i < 9; i++){
            float litAmount = (float) i / 8.0F;
            float red = litAmount * 0.7F + 0.3F;

            float green = litAmount * litAmount * 0.4F + 0.3F;
            float blue = 0.3F;

            if (green < 0.0F) {
                green = 0.0F;
            }

            if (blue < 0.0F) {
                blue = 0.0F;
            }

            int redInt = MathHelper.clamp(MathHelper.floor(red * 255), 0, 255);
            int greenInt = MathHelper.clamp(MathHelper.floor(green * 255), 0, 255);
            int blueInt = MathHelper.clamp(MathHelper.floor(blue * 255), 0, 255);

            COLORS[i] = MathHelper.color(redInt, greenInt, blueInt);
            // return 6579300;
        }
    }

    @Override
    public int getColor(BlockState state, IBlockDisplayReader reader, BlockPos pos, int color) {
        return COLORS[state.getValue(GunpowderBlock.BURNING)];
    }
}