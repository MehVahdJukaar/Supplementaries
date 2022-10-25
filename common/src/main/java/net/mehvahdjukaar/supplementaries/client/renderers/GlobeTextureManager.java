package net.mehvahdjukaar.supplementaries.client.renderers;


import com.google.common.collect.Maps;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.supplementaries.common.world.data.GlobeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;

import java.util.*;

public class GlobeTextureManager {

    private static final TextureManager TEXTURE_MANAGER = Minecraft.getInstance().getTextureManager();
    private static final Map<String, TextureInstance> TEXTURE_CACHE = Maps.newHashMap();

    public static void refreshTextures() {
        Level world = Minecraft.getInstance().level;
        if (world != null) {
            getTextureInstance(world, false).updateTexture(world);
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
        private final ResourceLocation dimensionId;
        private final boolean sepia;

        private TextureInstance(Level world, boolean sepia) {
            this.sepia = sepia;
            this.dimensionId = world.dimension().location();
            this.texture = new DynamicTexture(32, 16, false);
            this.updateTexture(world);
            this.textureLocation = TEXTURE_MANAGER.register("globe/" + dimensionId.toString().replace(":", "_"), this.texture);
            this.renderType = RenderType.entitySolid(textureLocation);
        }

        private void updateTexture(Level world) {
            var data = GlobeData.get(world);
            if (data == null) return;
            byte[][] pixels = data.globePixels;

            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 32; ++j) {
                    this.texture.getPixels().setPixelRGBA(j, i, -13061505);
                }
            }
            for (int y = 0; y < pixels.length; y++) {
                for (int x = 0; x < pixels[y].length; x++) {
                    this.texture.getPixels().setPixelRGBA(y, x,
                            getRGBA(pixels[y][x], this.dimensionId, this.sepia));
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


    private static final HashMap<ResourceLocation, List<Integer>> DIMENSION_COLOR_MAP = new HashMap<>();
    private static final List<Integer> SEPIA_COLORS = new ArrayList<>();

    /**
     * Refresh colors and textures
     */
    public static void refreshColorsAndTextures(ResourceManager manager) {

        DIMENSION_COLOR_MAP.clear();
        int targetColors = 13;

        for (var res : manager.listResources("textures/entity/globes/palettes",
                r -> r.getPath().endsWith(".png")).keySet()) {
            var l = SpriteUtils.parsePaletteStrip(manager, res, targetColors);
            String name = res.getPath();
            name = name.substring(name.lastIndexOf("/")+1).replace(".png","");
            if (name.equals("sepia")) {
                SEPIA_COLORS.clear();
                SEPIA_COLORS.addAll(l);
            } else {
                DIMENSION_COLOR_MAP.put(new ResourceLocation(name.replace(".", ":")), l);
            }
        }

        refreshTextures();
    }

    private static int getRGBA(byte b, ResourceLocation dimension, boolean sepia) {
        if (sepia) return SEPIA_COLORS.get(b);
        return DIMENSION_COLOR_MAP.getOrDefault(dimension,
                DIMENSION_COLOR_MAP.get(new ResourceLocation("overworld"))).get(b);
    }


}

