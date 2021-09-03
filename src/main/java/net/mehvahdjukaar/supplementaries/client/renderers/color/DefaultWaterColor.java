package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.biome.BiomeColors;

public class DefaultWaterColor implements IItemColor, IBlockColor{

    @Override
    public int getColor(ItemStack stack, int color) {
        return 0x3F76E4;
    }

    @Override
    public int getColor(BlockState state, IBlockDisplayReader reader, BlockPos pos, int color) {
        return reader != null && pos != null ? BiomeColors.getAverageWaterColor(reader, pos) : -1;
    }
}
