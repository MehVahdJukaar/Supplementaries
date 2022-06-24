package net.mehvahdjukaar.supplementaries.client.renderers;


import com.google.common.collect.Maps;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.world.data.GlobeData;
import net.mehvahdjukaar.supplementaries.common.world.data.GlobeDataGenerator;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

public class GlobeTextureManager {

    private static final TextureManager TEXTURE_MANAGER = Minecraft.getInstance().textureManager;
    private static final Map<String, TextureInstance> TEXTURE_CACHE = Maps.newHashMap();

    public static void refreshTextures() {
        Level world = Minecraft.getInstance().level;
        if (world != null) {
            getTextureInstance(world, false).updateTexture(world);
            getTextureInstance(world, true).updateTexture(world);
        }
    }

    public static RenderType getRenderType(Level world, boolean sepia) {
        return getTextureInstance(world, sepia).renderType;
    }

    private static TextureInstance getTextureInstance(Level world, boolean sepia) {
        return TEXTURE_CACHE.computeIfAbsent(getTextureId(world, sepia),
                (i) -> new TextureInstance(world, sepia));
    }

    private static String getTextureId(Level level, boolean sepia) {
        String id = level.dimension().location().getPath();
        if (sepia) id = id + "_sepia";
        return id;
    }

    private static class TextureInstance implements AutoCloseable {
        private final ResourceLocation textureLocation;
        private final DynamicTexture texture;
        private final RenderType renderType;
        private final String dimensionId;
        private final boolean sepia;

        private TextureInstance(Level world, boolean sepia) {
            this.sepia = sepia;
            this.dimensionId = world.dimension().location().toString();
            this.texture = new DynamicTexture(32, 16, false);
            this.updateTexture(world);
            this.textureLocation = TEXTURE_MANAGER.register("globe/" + dimensionId.replace(":", "_"), this.texture);
            this.renderType = RenderType.entitySolid(textureLocation);
        }

        private void updateTexture(Level world) {
            byte[][] pixels = GlobeData.get(world).globePixels;

            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 32; ++j) {
                    this.texture.getPixels().setPixelRGBA(j, i, -13061505);
                }
            }
            for (int y = 0; y < pixels.length; y++) {
                for (int x = 0; x < pixels[y].length; x++) {
                    this.texture.getPixels().setPixelRGBA(y, x, GlobeColors.getRGBA(pixels[y][x], this.dimensionId, this.sepia));
                }
            }
            this.texture.upload();
        }

        @Override
        public void close() {
            this.texture.close();
            TEXTURE_MANAGER.release(textureLocation);
        }
    }

    public static class GlobeColors {
        public static final HashMap<String, List<Integer>> DIMENSION_COLOR_MAP = new HashMap<>();
        public static final List<Integer> DEFAULT_COLORS = new ArrayList<>();
        public static final List<Integer> SEPIA_COLORS = new ArrayList<>();

        static {
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.BLACK, 0); //black
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.WATER, 0x23658d);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.WATER_S, 0x25527d);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.WATER_D, 0x1d396d);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.SUNKEN, 0x2d8a5c);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.GREEN, 0x34a03a);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.GREEN_S, 0x6ea14b);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.HOT_S, 0x89a83d);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.HOT, 0xb5ba65);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.COLD, 0xccd7d5);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.COLD_S, 0x83b4c6);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.ICEBERG, 0x2f83a2);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.MUSHROOM, 0x826e71);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.MUSHROOM_S, 0x8e8675);

            //TODO: finish this
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.TAIGA, 0x2d8a5c);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.MESA, 0xc28947);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.MESA_S, 0xba9f65);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.MOUNTAIN, 0xba9f65);
            DEFAULT_COLORS.add(GlobeDataGenerator.Col.MOUNTAIN_S, 0x769169);


            SEPIA_COLORS.add(GlobeDataGenerator.Col.BLACK, 0); //black
            SEPIA_COLORS.add(GlobeDataGenerator.Col.WATER, 0xbbb5a6);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.WATER_S, 0xa6a090);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.WATER_D, 0x908a78);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.SUNKEN, 0x857b65);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.GREEN, 0x766857);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.GREEN_S, 0x675a4a);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.HOT_S, 0x766857);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.HOT, 0x857b65);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.COLD, 0x766857);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.COLD_S, 0x857b65);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.ICEBERG, 0x908a78);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.MUSHROOM, 0x857b65);
            SEPIA_COLORS.add(GlobeDataGenerator.Col.MUSHROOM_S, 0x857b65);
        }

        public static List<List<String>> getDefaultConfig() {
            List<List<String>> l = new ArrayList<>();
            List<String> col = new ArrayList<>();
            col.add("minecraft:overworld");
            for (int i = 1; i < 13; i++) {
                col.add(Integer.toHexString(DEFAULT_COLORS.get((byte) i)));
            }
            l.add(col);

            l.add(Arrays.asList("minecraft:the_nether", "941818", "7b0000", "6a0400", "16615b", "941818", "ca4e06", "e66410", "f48522", "5a0000", "32333d", "118066", "100c1c"));
            l.add(Arrays.asList("minecraft:the_end", "061914", "000000", "2a0d2a", "000000", "d5da94", "cdc68b", "061914", "2a0d2a", "cdc68b", "000000", "eef6b4", "b286b2"));
            return l;
        }


        public static void refreshColorsFromConfig() {
            DIMENSION_COLOR_MAP.clear();
            try {
                List<? extends List<String>> customColors = ClientConfigs.block.GLOBE_COLORS.get();

                for (List<String> l : customColors) {
                    if (l.size() >= 13) {
                        String id = l.get(0);
                        List<Integer> col = new ArrayList<>();
                        //idk why this need to be here. can probably remove and access with -1
                        col.add(0);
                        for (int i = 1; i < 13; i++) {
                            int hex;
                            try {
                                hex = Integer.parseInt(l.get(i).replace("0x", ""), 16);
                            } catch (Exception e) {
                                Supplementaries.LOGGER.warn("failed to parse config 'globe_colors' (at dimension" + id + "). Try deleting them");
                                continue;
                            }
                            col.add(hex);
                        }
                        DIMENSION_COLOR_MAP.put(id, col);
                    }
                }
            } catch (Exception e) {
                Supplementaries.LOGGER.warn("failed to parse config globe_color configs. Try deleting them");
                DIMENSION_COLOR_MAP.put("minecraft:overworld", new ArrayList<>(DEFAULT_COLORS));
            }
        }

        private static int getRGB(byte b, String dimension, boolean sepia) {
            if (sepia) return SEPIA_COLORS.get(b);
            return DIMENSION_COLOR_MAP.getOrDefault(dimension, DEFAULT_COLORS).get(b);
        }

        public static int getRGBA(byte b, String dimension, boolean sepia) {
            int rgb = getRGB(b, dimension, sepia);
            return (255) << 24 | (rgb & 255) << 16 | (rgb >> 8 & 255) << 8 | (rgb >> 16 & 255);
        }
    }

}

