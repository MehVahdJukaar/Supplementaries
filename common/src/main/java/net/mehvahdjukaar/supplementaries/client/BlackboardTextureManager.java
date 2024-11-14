package net.mehvahdjukaar.supplementaries.client;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.systems.RenderSystem;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.items.components.BlackboardData;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

public class BlackboardTextureManager {

    private static final LoadingCache<BlackboardData, BlackboardVisuals> TEXTURE_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .removalListener(i -> {
                BlackboardVisuals value = (BlackboardVisuals) i.getValue();
                if (value != null) {
                    RenderSystem.recordRenderCall(value::close);
                }
            })
            .build(new CacheLoader<>() {
                @Override
                public BlackboardVisuals load(BlackboardData key) {
                    return null;
                }
            });

    public static BlackboardVisuals getInstance(BlackboardData key) {
        BlackboardVisuals textureInstance = TEXTURE_CACHE.getIfPresent(key);
        if (textureInstance == null) {
            textureInstance = new BlackboardVisuals(key.unpackPixels(), key.isGlow());
            TEXTURE_CACHE.put(key, textureInstance);
        }
        return textureInstance;
    }

    public static class BlackboardVisuals implements AutoCloseable {
        private static final int WIDTH = 16;

        //models for each direction
        private final Map<Direction, List<BakedQuad>> quadsCache = new EnumMap<>(Direction.class);
        private final byte[][] pixels;
        private final boolean glow;
        //he be lazy
        @Nullable
        private DynamicTexture texture;
        @Nullable
        private RenderType renderType;
        @Nullable
        private ResourceLocation textureLocation;

        private BlackboardVisuals(byte[][] pixels, boolean glow) {
            this.pixels = pixels;
            this.glow = glow;
        }

        public byte[][] getPixels() {
            return pixels;
        }

        public boolean isGlow() {
            return glow;
        }

        //cant initialize right away since this texture can be created from worked main tread during model bake since it needs getQuads

        private void initializeTexture() {
            //no need for mipmap, we use block sheet
            this.texture = new DynamicTexture(WIDTH, WIDTH, false);
            for (int y = 0; y < pixels.length && y < WIDTH; y++) {
                for (int x = 0; x < pixels[y].length && x < WIDTH; x++) { //getColoredPixel(BlackboardBlock.colorFromByte(pixels[x][y]),x,y)
                    this.texture.getPixels().setPixelRGBA(x, y, getColoredPixel(pixels[x][y], x, y));
                }
            }
            this.texture.upload();

            //texture manager has its own internal id
            this.textureLocation = Minecraft.getInstance().getTextureManager()
                    .register("blackboard/", this.texture);
            this.renderType = RenderType.entitySolid(textureLocation);
        }

        //helper methods
        private static int getColoredPixel(byte i, int x, int y) {
            int offset = i > 0 ? 16 : 0;
            int tint = BlackboardBlock.colorFromByte(i);
            TextureAtlas textureMap = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
            TextureAtlasSprite sprite = textureMap.getSprite(ModTextures.BLACKBOARD_TEXTURE);
            return getTintedColor(sprite, x, y, offset, tint);
        }

        private static int getTintedColor(TextureAtlasSprite sprite, int x, int y, int offset, int tint) {
            if (sprite == null || sprite.contents().getFrameCount() == 0) return -1;
            int tintR = FastColor.ABGR32.red(tint);
            int tintG = FastColor.ABGR32.green(tint);
            int tintB = FastColor.ABGR32.blue(tint);

            int pixel = ClientHelper.getPixelRGBA(sprite, 0, Math.min(sprite.contents().width() - 1, x + offset), Math.min(sprite.contents().height() - 1, y));

            int totalB = FastColor.ARGB32.blue(pixel);
            int totalG = FastColor.ARGB32.green(pixel);
            int totalR = FastColor.ARGB32.red(pixel);
            //image has them inverted
            return FastColor.ARGB32.color(255, totalR * tintR / 255,totalG * tintG / 255, totalB * tintB / 255);
        }

        @NotNull
        public List<BakedQuad> getOrCreateModel(Direction dir, BiFunction<BlackboardVisuals, Direction, List<BakedQuad>> modelFactory) {
            return quadsCache.computeIfAbsent(dir, d -> modelFactory.apply(this, d));
        }

        @NotNull
        public ResourceLocation getTextureLocation() {
            if (textureLocation == null) {
                //I can only initialize it here since this is guaranteed to be on render thread
                this.initializeTexture();
            }
            return textureLocation;
        }

        @NotNull
        public RenderType getRenderType() {
            if (renderType == null) {
                //I can only initialize it here since this is guaranteed to be on render thread
                this.initializeTexture();
            }
            return renderType;
        }

        //should be called when cache expires
        @Override
        public void close() {
            if (texture != null) this.texture.close();
            if (textureLocation != null) {
                Minecraft.getInstance().getTextureManager().release(textureLocation);
            }
        }
    }
}

