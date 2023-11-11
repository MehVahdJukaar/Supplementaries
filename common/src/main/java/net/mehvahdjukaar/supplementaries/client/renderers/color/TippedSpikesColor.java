package net.mehvahdjukaar.supplementaries.client.renderers.color;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BambooSpikesBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CompatHandler;
import net.mehvahdjukaar.supplementaries.integration.QuarkCompat;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

public class TippedSpikesColor implements BlockColor, ItemColor {

    //not using concurrent hashmap cause its slow since its blocking. only one thread should access these anyways but we never know
    private static final ThreadLocal<Int2IntMap> CACHED_COLORS_0 = ThreadLocal.withInitial(Int2IntOpenHashMap::new);
    private static final ThreadLocal<Int2IntMap> CACHED_COLORS_1 = ThreadLocal.withInitial(Int2IntOpenHashMap::new);

    private static int getCachedColor(int base, int tint) {
        return switch (tint) {
            default -> -1;
            case 1 -> CACHED_COLORS_0.get().computeIfAbsent(base, b -> getProcessedColor(base, 0));
            case 2 -> CACHED_COLORS_1.get().computeIfAbsent(base, b -> getProcessedColor(base, 1));
        };
    }

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
        if (world != null && pos != null) {
            if (world.getBlockEntity(pos) instanceof BambooSpikesBlockTile tile) {
                int color = tile.getColor();
                return getCachedColor(color, tint);
            }
            //not actually sure why I need this since quark seems to handle moving tiles pretty well
            else if (CompatHandler.QUARK) {
                if (world instanceof Level level) {
                    if (QuarkCompat.getMovingBlockEntity(pos, state, level) instanceof BambooSpikesBlockTile tile) {
                        int color = tile.getColor();
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
        return getCachedColor(PotionUtils.getColor(stack), tint);
    }

    private static int getProcessedColor(int rgb, int tint) {
        var hsl = new RGBColor(rgb).asHSL();
        float h = hsl.hue();
        if (tint == 1) {
            boolean b = h > 0.16667f && h < 0.6667f;
            float i = b ? -0.04f : +0.04f;
            h = (h + i) % 1f;
        }

        hsl = ColorHelper.prettyfyColor(hsl.withHue(h));
        float s = hsl.saturation();
        //0.7,0.6
        s = tint == 0 ? (s * 0.81f) : s * 0.74f;
        return hsl.withSaturation(s).asRGB().toInt();
    }
}
