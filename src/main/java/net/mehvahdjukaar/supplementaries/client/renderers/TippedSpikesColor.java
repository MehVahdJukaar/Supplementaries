package net.mehvahdjukaar.supplementaries.client.renderers;

import net.mehvahdjukaar.supplementaries.block.tiles.BambooSpikesBlockTile;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;

public class TippedSpikesColor implements IBlockColor, IItemColor {

    @Override
    public int getColor(BlockState state, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tint) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof BambooSpikesBlockTile){
            return getProcessedColor(((BambooSpikesBlockTile) te).getColor(), tint);
        }
        return 0xffffff;
    }

    @Override
    public int getColor(ItemStack stack, int tint) {
        if(tint==0)return 0xffffff;

        return getProcessedColor(PotionTooltipHelper.getColor(stack.getChildTag("BlockEntityTag")), tint-1);
    }

    private int getProcessedColor(int rgb, int tint){
        //float[] hsb = Color.RGBtoHSB(r,g,b,null);
        //int rgb2 = Color.HSBtoRGB(hsb[0],0.75f,0.85f);
        float[] hsl = HSLColor.rgbToHsl(rgb);
        if(tint==1){
            float h = hsl[0];
            boolean b = h>0.16667f&&h<0.6667f;
            float i = b? -0.04f:+0.04f;
            hsl[0] = (h+i)%1f;
        }

        hsl = HSLColor.postProcess(hsl);
        float h = hsl[0];
        float s = hsl[1];
        float l = hsl[2];
        //0.7,0.6
        s = tint==0?((s*0.81f)):s*0.74f;
        return HSLColor.hslToRgb(h, s, l);
    }
}
