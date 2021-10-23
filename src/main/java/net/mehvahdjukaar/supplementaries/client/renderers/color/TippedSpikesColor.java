package net.mehvahdjukaar.supplementaries.client.renderers.color;

import net.mehvahdjukaar.supplementaries.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.compat.CompatHandler;
import net.mehvahdjukaar.supplementaries.compat.quark.QuarkPistonPlugin;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TippedSpikesColor implements BlockColor, ItemColor {
    public static Map<Integer, Integer> cachedColors0 = new HashMap<>();
    public static Map<Integer, Integer> cachedColors1 = new HashMap<>();

    //TODO: figure out if this is actually faster. seems to me about the same, maybe slightly faster this way
    public static int getCachedColor(int base, int tint) {
        if (tint == 0) {
            if (!cachedColors0.containsKey(base)) {
                int c = getProcessedColor(base, tint);
                cachedColors0.put(base, c);
                return c;
            } else {
                return cachedColors0.get(base);
            }
        } else {
            if (!cachedColors1.containsKey(base)) {
                int c = getProcessedColor(base, tint);
                cachedColors1.put(base, c);
                return c;
            } else {
                return cachedColors1.get(base);
            }
        }
    }


    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
        if (world != null && pos != null) {
            if (world.getBlockEntity(pos) instanceof BambooSpikesBlockTile tile) {
                int color = tile.getColor();
                //return getProcessedColor(color, tint);
                return getCachedColor(color, tint);
            }
            //not actually sure why I need this since quark seems to handle moving tiles pretty well
            else if (CompatHandler.quark) {
                if (world instanceof Level) {
                    te = QuarkPistonPlugin.getMovingTile(pos, (World) world);
                    if (te instanceof BambooSpikesBlockTile) {
                        int color = ((BambooSpikesBlockTile) te).getColor();
                        //return getProcessedColor(color, tint);
                        return getCachedColor(color, tint);
                    }
                }
            }
        }
        return 0xffffff;
    }

    @Override
    public int getColor(ItemStack stack, int tint) {
        if (tint == 0) return 0xffffff;
        return getCachedColor(PotionUtils.getColor(stack), tint - 1);
        //return getProcessedColor(PotionUtils.getColor(stack), tint-1);
    }

    public static int getProcessedColor(int rgb, int tint) {
        //float[] hsb = Color.RGBtoHSB(r,g,b,null);
        //int rgb2 = Color.HSBtoRGB(hsb[0],0.75f,0.85f);
        float[] hsl = HSLColor.rgbToHsl(rgb);
        if (tint == 1) {
            float h = hsl[0];
            boolean b = h > 0.16667f && h < 0.6667f;
            float i = b ? -0.04f : +0.04f;
            hsl[0] = (h + i) % 1f;
        }

        hsl = HSLColor.postProcess(hsl);
        float h = hsl[0];
        float s = hsl[1];
        float l = hsl[2];
        //0.7,0.6
        s = tint == 0 ? ((s * 0.81f)) : s * 0.74f;
        return HSLColor.hslToRgb(h, s, l);
    }
}
