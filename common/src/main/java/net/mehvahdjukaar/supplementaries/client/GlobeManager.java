package net.mehvahdjukaar.supplementaries.client;


import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.resources.textures.SpriteUtils;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.common.misc.globe.GlobeData;
import net.mehvahdjukaar.supplementaries.common.utils.Credits;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.mehvahdjukaar.supplementaries.reg.ModTextures.*;

public class GlobeManager {

    private static final Map<String, TextureInstance> TEXTURE_CACHE = Maps.newHashMap();

    private static final HashMap<ResourceLocation, IntList> DIMENSION_COLORS = new HashMap<>();
    private static final IntList SEPIA_COLORS = new IntArrayList();

    private static final Map<String, GlobeRenderData> NAME_CACHE = new HashMap<>();
    private static final Map<String, Float> MODEL_ID_MAP = new HashMap<>();

    public static final List<ResourceLocation> TEXTURES = new ArrayList<>();


    public static void refreshTextures() {
        TEXTURE_CACHE.clear();
    }

    private static TextureInstance getTextureInstance(Level world, boolean sepia) {
        return TEXTURE_CACHE.computeIfAbsent(getTextureIdPerDimension(world, sepia),
                i -> new TextureInstance(world, sepia));
    }

    private static String getTextureIdPerDimension(Level level, boolean sepia) {
        String id = level.dimension().location().getPath();
        if (sepia) id = id + "_sepia";
        return id;
    }

    public static GlobeRenderData computeRenderData(boolean sheared, @Nullable Component customName) {
        if (sheared) {
            return SpecialGlobe.SHEARED;
        } else if (customName != null) {
            var specialGlobe = NAME_CACHE.get(customName.getString().toLowerCase(Locale.ROOT));
            if (specialGlobe != null) return specialGlobe;
        }
        return DEFAULT_DATA;
    }

    //for supporter globe item texture
    //Disabled because it requires too much maintenance
    public static Float getNamedGlobeTextureID(String text) {
        return Float.NEGATIVE_INFINITY;
        //return MODEL_ID_MAP.getOrDefault(text.toLowerCase(Locale.ROOT), Float.NEGATIVE_INFINITY);
    }


    private static class TextureInstance implements AutoCloseable {
        private final ResourceLocation textureLocation;
        private final DynamicTexture texture;
        private final ResourceLocation dimensionId;
        private final boolean sepiaColored;

        private TextureInstance(Level world, boolean sepia) {
            this.sepiaColored = sepia;
            this.dimensionId = world.dimension().location();
            RenderUtil.setDynamicTexturesToUseMipmap(true);
            this.texture = new DynamicTexture(32, 16, false);
            RenderUtil.setDynamicTexturesToUseMipmap(false);
            this.updateTexture(world);
            this.textureLocation = Minecraft.getInstance().getTextureManager()
                    .register("globe/" + dimensionId.toString().replace(":", "_"), this.texture);
        }

        private void updateTexture(Level world) {
            GlobeData data = ModRegistry.GLOBE_DATA.getData(world);

            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 32; ++j) {
                    this.texture.getPixels().setPixelRGBA(j, i, -13061505);
                }
            }
            int width = data.getTextureWidth();
            int height = data.getTextureHeight();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    this.texture.getPixels().setPixelRGBA(x, y,
                            getRGBA(data.getPixel(x,y), this.dimensionId, this.sepiaColored));
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
            IntList l = DIMENSION_COLORS.getOrDefault(dimension, DIMENSION_COLORS.get(ResourceLocation.withDefaultNamespace("overworld")));
            if (l != null) {
                return l.getInt(b);
            }
            return 1;
        }
    }


    /**
     * Refresh colors and textures
     */
    public static void refreshColorsAndTextures(ResourceManager manager) {

        DIMENSION_COLORS.clear();
        int targetColors = 13;

        for (var res : manager.listResources("textures/entity/globes/palettes",
                r -> r.getPath().endsWith(".png")).keySet()) {
            var l = SpriteUtils.parsePaletteStrip(manager, res, targetColors);
            String name = res.getPath();
            name = name.substring(name.lastIndexOf("/") + 1).replace(".png", "");
            if (name.equals("sepia")) {
                SEPIA_COLORS.clear();
                SEPIA_COLORS.addAll(l);
            } else {
                DIMENSION_COLORS.put(ResourceLocation.tryParse(name.replace(".", ":")), new IntArrayList(l));
            }
        }
        if (DIMENSION_COLORS.isEmpty()) {
            Supplementaries.LOGGER.error("Could not find any globe palette in textures/entity/globes/palettes");
        }

        if (SEPIA_COLORS.isEmpty())
            throw new RuntimeException("Could not find sepia globe palette in textures/entity/globes/palettes");

        recomputeCache();

        refreshTextures();
    }

    // remove this. its very random
    private enum SpecialGlobe implements GlobeRenderData {
        FLAT(Component.translatable("globe.supplementaries.flat"), GLOBE_FLAT_TEXTURE, GLOBE_FLAT_TEXTURE_SEPIA, Model.FLAT,
                "flat", "flat earth"),
        MOON(Component.translatable("globe.supplementaries.moon"), GLOBE_MOON_TEXTURE, GLOBE_MOON_TEXTURE,
                "moon", "luna", "selene", "cynthia"),
        EARTH(Component.translatable("globe.supplementaries.earth"), GLOBE_EARTH_TEXTURE, GLOBE_EARTH_TEXTURE_SEPIA,
                "earth", "terra", "gaia", "gaea", "tierra", "tellus", "terre"),
        SUN(Component.translatable("globe.supplementaries.sun"), GLOBE_SUN_TEXTURE, GLOBE_SUN_TEXTURE,
                "sun", "sol", "helios"),
        SHEARED(Component.literal("sheared"), GLOBE_SHEARED_TEXTURE, GLOBE_SHEARED_SEPIA_TEXTURE,
                Model.SHEARED),
        ROUND(Component.translatable("globe.supplementaries.round"), GLOBE_EARTH_TEXTURE, GLOBE_EARTH_TEXTURE_SEPIA,
                Model.ROUND, "round", "sphere", "spherical"),
        ;

        SpecialGlobe(Component tr, ResourceLocation texture, ResourceLocation textureSepia, String... key) {
            this(tr, texture, textureSepia, Model.GLOBE, key);
        }

        SpecialGlobe(Component tr, ResourceLocation texture,
                     ResourceLocation textureSepia, Model model, String... keywords) {
            this.keyWords = keywords;
            this.transKeyWord = tr;
            this.texture = texture;
            this.textureSepia = textureSepia;
            this.model = model;
        }

        private final String[] keyWords;
        private final Component transKeyWord;
        private final ResourceLocation texture;
        private final ResourceLocation textureSepia;
        private final Model model;

        public @NotNull ResourceLocation getTexture(boolean sepia) {
            if (this == ROUND) {
                return DEFAULT_DATA.getTexture(sepia);
            }
            return sepia ? this.textureSepia : this.texture;
        }

        @Override
        public Model getModel(boolean sepia) {
            return this.model;
        }
    }

    private static void recomputeCache() {
        NAME_CACHE.clear();
        for (SpecialGlobe type : SpecialGlobe.values()) {
            if (type.keyWords.length == 0) continue;
            if (type.transKeyWord != null && !type.transKeyWord.getString().isEmpty()) {
                NAME_CACHE.put(type.transKeyWord.getString().toLowerCase(Locale.ROOT), type);
            }
            for (String s : type.keyWords) {
                if (!s.isEmpty()) {
                    NAME_CACHE.put(s, type);
                }
            }
        }

        for (var g : Credits.INSTANCE.globes().entrySet()) {
            var path = g.getValue();
            Model model = Model.GLOBE;
            //I should have stopped giving away special model globes from the start... this uglifies everything
            if (path.getPath().contains("globe_wais")) {
                model = Model.SNOW;
            }
            NAME_CACHE.put(g.getKey(), SimpleData.of(model, path));
        }
        TEXTURES.clear();
        Set<ResourceLocation> allTextures = new HashSet<>();
        NAME_CACHE.values().forEach(o -> {
            if (o == DEFAULT_DATA || o == SpecialGlobe.ROUND) return; //skip default data
            ResourceLocation t1 = o.getTexture(false);
            allTextures.add(t1);
            ResourceLocation t2 = o.getTexture(true);
            if (t1 != t2) allTextures.add(t2);
        });
        TEXTURES.addAll(allTextures);
        //DISABLED for now. requires too much maintenance
        //  TEXTURES.sort(Comparator.comparing(ResourceLocation::toString));
        // NAME_CACHE.forEach((key, value) -> MODEL_ID_MAP.put(key, (float) TEXTURES.indexOf(value.texture)));
    }

    public enum Model {
        GLOBE, FLAT, SNOW, SHEARED, ROUND
    }


    private record SimpleData(Model model, @NotNull ResourceLocation texture) implements GlobeRenderData {

        public static SimpleData of(Model model, @NotNull ResourceLocation texture) {
            return new SimpleData(model, texture);
        }

        @Override
        public Model getModel(boolean sepia) {
            return model;
        }

        @Override
        public @NotNull ResourceLocation getTexture(boolean sepia) {
            return texture;
        }
    }

    //random globe
    public static final GlobeRenderData DEFAULT_DATA = new GlobeRenderData() {
        @Override
        public Model getModel(boolean sepia) {
            return Model.GLOBE;
        }

        @Override
        public @NotNull ResourceLocation getTexture(boolean sepia) {
            if (!ClientConfigs.Blocks.GLOBE_RANDOM.get()) {
                return SpecialGlobe.EARTH.getTexture(sepia);
            }
            Level level = Minecraft.getInstance().level;
            if (level == null) {
                return GLOBE_EARTH_TEXTURE;
            }
            return getTextureInstance(level, sepia).textureLocation;
        }

    };
}

