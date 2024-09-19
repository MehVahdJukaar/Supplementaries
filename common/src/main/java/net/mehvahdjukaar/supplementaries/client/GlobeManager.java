package net.mehvahdjukaar.supplementaries.client;


import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.globe.GlobeData;
import net.mehvahdjukaar.supplementaries.common.utils.Credits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.mehvahdjukaar.supplementaries.reg.ModTextures.*;

public class GlobeManager {

    private static final Map<String, TextureInstance> TEXTURE_CACHE = Maps.newHashMap();

    private static final HashMap<ResourceLocation, IntList> DIMENSION_COLOR_MAP = new HashMap<>();
    private static final IntList SEPIA_COLORS = new IntArrayList();

    public static void refreshTextures() {
        TEXTURE_CACHE.clear();
    }

    public static RenderType getRenderType(Level world, boolean sepia) {
        return getTextureInstance(world, sepia).renderType;
    }

    private static TextureInstance getTextureInstance(Level world, boolean sepia) {
        return TEXTURE_CACHE.computeIfAbsent(getTextureId(world, sepia),
                i -> new TextureInstance(world, sepia));
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
            RenderUtil.setDynamicTexturesToUseMipmap(true);
            this.texture = new DynamicTexture(32, 16, false);
            RenderUtil.setDynamicTexturesToUseMipmap(false);
            this.updateTexture(world);
            this.textureLocation = Minecraft.getInstance().getTextureManager()
                    .register("globe/" + dimensionId.toString().replace(":", "_"), this.texture);
            this.renderType = RenderUtil.getEntitySolidMipmapRenderType(textureLocation);
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
            RenderUtil.setDynamicTexturesToUseMipmap(true);
            this.texture.upload();
            RenderUtil.setDynamicTexturesToUseMipmap(false);
        }

        @Override
        public void close() {
            this.texture.close();
            Minecraft.getInstance().getTextureManager().release(textureLocation);
        }

        private static int getRGBA(byte b, ResourceLocation dimension, boolean sepia) {
            if (sepia) return SEPIA_COLORS.getInt(b);
            IntList l = DIMENSION_COLOR_MAP.getOrDefault(dimension, DIMENSION_COLOR_MAP.get(ResourceLocation.withDefaultNamespace("overworld")));
            if(l != null){
               return l.getInt(b);
            }
            return 1;
        }
    }


    /**
     * Refresh colors and textures
     */
    public static void refreshColorsAndTextures(ResourceManager manager) {
        recomputeCache();

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
                DIMENSION_COLOR_MAP.put(ResourceLocation.tryParse(name.replace(".", ":")), new IntArrayList(l));
            }
        }
        if(DIMENSION_COLOR_MAP.isEmpty()){
            Supplementaries.LOGGER.error("Could not find any globe palette in textures/entity/globes/palettes");
        }

        refreshTextures();
    }

    // remove this. its very random
    public enum Type {
        FLAT(new String[]{"flat", "flat earth"}, Component.translatable("globe.supplementaries.flat"), GLOBE_FLAT_TEXTURE),
        MOON(new String[]{"moon", "luna", "selene", "cynthia"},
                Component.translatable("globe.supplementaries.moon"), GLOBE_MOON_TEXTURE),
        EARTH(new String[]{"earth", "terra", "gaia", "gaea", "tierra", "tellus", "terre"},
                Component.translatable("globe.supplementaries.earth"), GLOBE_TEXTURE),
        SUN(new String[]{"sun", "sol", "helios"},
                Component.translatable("globe.supplementaries.sun"), GLOBE_SUN_TEXTURE);

        Type(String[] key, Component tr, ResourceLocation res) {
            this.keyWords = key;
            this.transKeyWord = tr;
            this.texture = res;
        }

        private final String[] keyWords;
        private final Component transKeyWord;
        private final ResourceLocation texture;

        public ResourceLocation getTexture() {
            return texture;
        }
    }


    private static final Map<String, Pair<Model, ResourceLocation>> NAME_CACHE = new HashMap<>();
    private static final Map<String, Float> MODEL_ID_MAP = new HashMap<>();
    private static final List<ResourceLocation> TEXTURES = new ArrayList<>();

    public static void recomputeCache() {
        NAME_CACHE.clear();
        for (Type type : Type.values()) {
            Model model = type == Type.FLAT ? Model.FLAT : Model.GLOBE;
            var pair = Pair.of(model, type.texture);
            if (type.transKeyWord != null && !type.transKeyWord.getString().equals("")) {
                NAME_CACHE.put(type.transKeyWord.getString().toLowerCase(Locale.ROOT), pair);
            }
            for (String s : type.keyWords) {
                if (!s.equals("")) {
                    NAME_CACHE.put(s, pair);
                }
            }
        }

        for (var g : Credits.INSTANCE.globes().entrySet()) {
            var path = g.getValue();
            Model model = Model.GLOBE;
            if (path.getPath().contains("globe_wais")) {
                model = Model.SNOW;
            }
            NAME_CACHE.put(g.getKey(), Pair.of(model, path));
        }
        TEXTURES.clear();
        NAME_CACHE.values().forEach(o -> {
            if(!TEXTURES.contains(o.getSecond())) TEXTURES.add(o.getSecond());
        });
        Collections.sort(TEXTURES);
        MODEL_ID_MAP.clear();
        NAME_CACHE.forEach((key, value) -> MODEL_ID_MAP.put(key, (float) TEXTURES.indexOf(value.getSecond())));
    }

    @Nullable
    public static Pair<Model, ResourceLocation> getModelAndTexture(String text) {
        return NAME_CACHE.get(text.toLowerCase(Locale.ROOT));
    }

    public static Float getTextureID(String text) {
        return MODEL_ID_MAP.getOrDefault(text.toLowerCase(Locale.ROOT), Float.NEGATIVE_INFINITY);
    }

    public enum Model {
        GLOBE, FLAT, SNOW, SHEARED
    }

}

