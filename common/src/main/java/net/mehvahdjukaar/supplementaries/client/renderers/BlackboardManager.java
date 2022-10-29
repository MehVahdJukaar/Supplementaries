package net.mehvahdjukaar.supplementaries.client.renderers;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.moonlight.api.platform.ClientPlatformHelper;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.security.Provider;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlackboardManager {

    private static final TextureManager TEXTURE_MANAGER = Minecraft.getInstance().getTextureManager();

    private static final LoadingCache<BlackboardKey, TextureInstance> TEXTURE_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .removalListener(i -> {
                TextureInstance value = (TextureInstance) i.getValue();
                if (value != null) value.close();
            })
            .build(new CacheLoader<>() {
                @Override
                public TextureInstance load(BlackboardKey key) {
                    return null;
                }
            });

    public static TextureInstance getBlackboardInstance(BlackboardKey key) {
        TextureInstance textureInstance = TEXTURE_CACHE.getIfPresent(key);
        if (textureInstance == null) {
            textureInstance = new TextureInstance(BlackboardBlockTile.unpackPixels(key.values), key.glow);
            TEXTURE_CACHE.put(key, textureInstance);
        }
        return textureInstance;
    }

    public static class BlackboardKey {
        private final long[] values;
        private final boolean glow;

        BlackboardKey(long[] packed, boolean glowing) {
            values = packed;
            glow = glowing;
        }

        public static BlackboardKey of(long[] packPixels, boolean glowing) {
            return new BlackboardKey(packPixels, glowing);
        }

        public static BlackboardKey of(long[] packPixels) {
            return new BlackboardKey(packPixels, false);
        }

        @Override
        public boolean equals(Object another) {
            if (another == this) {
                return true;
            }
            if (another == null) {
                return false;
            }
            if (another.getClass() != this.getClass()) {
                return false;
            }
            BlackboardKey key = (BlackboardKey) another;
            return Arrays.equals(this.values, key.values) && glow == key.glow;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.values);
        }
    }


    public static class TextureInstance implements AutoCloseable {
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

        private TextureInstance(byte[][] pixels, boolean glow) {
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
            this.texture = new DynamicTexture(WIDTH, WIDTH, false);

            for (int y = 0; y < pixels.length && y < WIDTH; y++) {
                for (int x = 0; x < pixels[y].length && x < WIDTH; x++) { //getColoredPixel(BlackboardBlock.colorFromByte(pixels[x][y]),x,y)
                    this.texture.getPixels().setPixelRGBA(x, y, getColoredPixel(pixels[x][y], x, y));
                }
            }
            this.texture.upload();

            //texture manager has its own internal id
            this.textureLocation = TEXTURE_MANAGER.register("blackboard/", this.texture);
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
            if (sprite == null || sprite.getFrameCount() == 0) return -1;
            int tintR = tint >> 16 & 255;
            int tintG = tint >> 8 & 255;
            int tintB = tint & 255;

            int pixel = ClientPlatformHelper.getPixelRGBA(sprite, 0, Math.min(sprite.getWidth() - 1, x + offset), Math.min(sprite.getHeight() - 1, y));

            // this is in 0xAABBGGRR format, not the usual 0xAARRGGBB.
            int totalB = pixel >> 16 & 255;
            int totalG = pixel >> 8 & 255;
            int totalR = pixel & 255;
            return NativeImage.combine(255, totalB * tintB / 255, totalG * tintG / 255, totalR * tintR / 255);
        }

        @Nonnull
        public List<BakedQuad> getOrCreateModel(Direction dir, Supplier<List<BakedQuad>> modelFactory) {
            return quadsCache.computeIfAbsent(dir, p-> modelFactory.get());
        }

        @Nonnull
        public ResourceLocation getTextureLocation() {
            if (textureLocation == null) {
                //I can only initialize it here since this is guaranteed to be on render thread
                this.initializeTexture();
            }
            return textureLocation;
        }

        @Nonnull
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
            if (textureLocation != null) TEXTURE_MANAGER.release(textureLocation);
        }
    }


}

