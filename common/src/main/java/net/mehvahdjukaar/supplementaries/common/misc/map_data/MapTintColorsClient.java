package net.mehvahdjukaar.supplementaries.common.misc.map_data;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.LABColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.material.MapColor;

import java.util.Arrays;
import java.util.Map;

public class MapTintColorsClient {
    private static final int[] IND2COLOR_BUFFER = new int[256 * 4];
    private static final Map<Pair<MapTintColorsHandler.BlockAndBiome, Integer>, Integer> GLOBAL_COLOR_CACHE = new Object2IntOpenHashMap<>();


    public static void clearColorCache() {
        GLOBAL_COLOR_CACHE.clear();
        System.arraycopy(new int[256 * 4], 0, IND2COLOR_BUFFER, 0, 256 * 4);
    }


    public static void processTexture(MapTintColorsHandler.ColorData data, NativeImage texture, int startX, int startY, byte[] colors) {
        if (!ClientConfigs.Tweaks.COLORED_MAPS.get() || data.isEmpty()) return;
        boolean tallGrass = ClientConfigs.Tweaks.TALL_GRASS_COLOR_CHANGE.get();
        boolean accurateConfig = ClientConfigs.Tweaks.ACCURATE_COLORED_MAPS.get();
        // with this, we also prevent repeated map cache achess
        if (!accurateConfig) Arrays.fill(IND2COLOR_BUFFER, 0);
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();

        for (int x = 0; x < 128; ++x) {
            for (int z = 0; z < 128; ++z) {
                int index = data.paletteIndex(x, z);
                //exit early
                if (index == 0) continue;

                int newTint = -1;
                int k = x + z * 128;
                byte packedId = colors[k];
                int brightnessInd = packedId & 3;
                //exit early if we already know the color of this index
                if (!accurateConfig) {
                    int alreadyKnownColor = IND2COLOR_BUFFER[index + brightnessInd * 256];
                    if (alreadyKnownColor != 0) newTint = alreadyKnownColor;
                }
                //else compute it
                if (newTint == -1) {
                    MapTintColorsHandler.BlockAndBiome e = data.getEntry(x, z);
                    data.lastEntryHack = e;

                    if (e == null) continue;
                    Block block = e.block();


                    if (accurateConfig) {
                        BlockPos pos = new BlockPos(x, 64, z); //this is bad. don't want to send extra data tho
                        int tint = blockColors.getColor(block.defaultBlockState(),
                                data, pos, 0);
                        if (tint != -1) {
                            newTint = postProcessTint(tallGrass, packedId, block, tint);
                        }
                    } else {
                        newTint = GLOBAL_COLOR_CACHE.computeIfAbsent(Pair.of(e, brightnessInd), n -> {
                            BlockPos pos = new BlockPos(0, 64, 0);
                            int tint = blockColors.getColor(block.defaultBlockState(), data, pos, 0);
                            return postProcessTint(tallGrass, packedId, block, tint);
                        });
                        //cache for this cycle
                        IND2COLOR_BUFFER[index + brightnessInd * 256] = newTint;
                    }
                }

                if (newTint != -1) {
                    texture.setPixelRGBA(startX + x, startY + z, newTint);
                }
            }
        }
    }

    private static int postProcessTint(boolean tg, byte packedId, Block block, int tint) {

        float lumIncrease = 1.3f;
        MapColor mapColor = MapColor.byId((packedId & 255) >> 2);
        if (mapColor == MapColor.WATER) {
            lumIncrease = 2f;
        }
        /*
        else if(mapColor == MapColor.PLANT){
            if(tint == blockColors.getColor(Blocks.GRASS.defaultBlockState(), this, pos, 0)){
                 packedId = MapColor.GRASS.getPackedId(MapColor.Brightness.byId(packedId & 3));
            }
        }*/
        else if (mapColor == MapColor.PLANT && block instanceof BushBlock && tg) {
            packedId = MapColor.GRASS.getPackedId(MapColor.Brightness.byId(packedId & 3));
        }
        int color = MapColor.getColorFromPackedId(packedId);

        tint = ColorUtils.swapFormat(tint);
        RGBColor tintColor = new RGBColor(tint);
        LABColor c = new RGBColor(color).asLAB();
        RGBColor gray = c.multiply(lumIncrease, 0, 0, 1).asRGB();
        return gray.multiply(tintColor.red(), tintColor.green(), tintColor.blue(), 1)
                .asHSL().multiply(1, 1.3f, 1, 1).asRGB().toInt();
    }


}
