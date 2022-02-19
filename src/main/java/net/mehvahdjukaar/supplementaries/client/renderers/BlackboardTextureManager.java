package net.mehvahdjukaar.supplementaries.client.renderers;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.platform.NativeImage;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.common.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.utils.Textures;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class BlackboardTextureManager {

    public static BlackboardTextureManager INSTANCE = null;
    //private final AtlasTexture textureAtlas;
    private final TextureManager textureManager;

    private long lastID = 0;

    //will roll over at some point overriding possible textures. Not really a problem since it's ver unlikely somebody will generate 2^64 blackboards in a session
    public long bindNextID() {
        return ++lastID;
    }

    //TODO: call close() on DynamicTextures to prevent memory leak
    private final LoadingCache<BlackboardKey, TextureInstance> blackboardTextures = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .build(new CacheLoader<BlackboardKey, TextureInstance>() {
                @Override
                public TextureInstance load(BlackboardKey key) {
                    return null;
                }
            });

    public static void init(TextureManager textureManager) {
        INSTANCE = new BlackboardTextureManager(textureManager);
    }

    public BlackboardTextureManager(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public TextureInstance getBlackboardInstance(BlackboardKey key) {
        TextureInstance textureInstance = this.blackboardTextures.getIfPresent(key);
        if (textureInstance == null) {
            textureInstance = new TextureInstance(BlackboardBlockTile.unpackPixels(key.values), bindNextID());
            this.blackboardTextures.put(key, textureInstance);
        }
        return textureInstance;
    }

    public TextureInstance getBlackboardInstance(long[] packed) {
        return getBlackboardInstance(new BlackboardKey(packed));
    }

    public TextureInstance getBlackboardInstance(BlackboardBlockTile tile) {
        return getBlackboardInstance(tile.getTextureKey());
    }

    public static class BlackboardKey {
        private final long[] values;

        public BlackboardKey(long[] packed) {
            values = packed;
        }

        public BlackboardKey(byte[][] pixels) {
            values = BlackboardBlockTile.packPixels(pixels);
            ;
        }

        public byte[][] unpackValues(){
            return BlackboardBlockTile.unpackPixels(values);
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
            return Arrays.equals(this.values, key.values);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.values);
        }
    }


    public class TextureInstance implements AutoCloseable {
        private static final int WIDTH = 16;
        private final byte[][] pixels;
        private final long id;
        //he be lazy
        @Nullable
        private DynamicTexture texture;
        @Nullable
        private RenderType renderType;
        @Nullable
        private ResourceLocation textureLocation;
        private Map<Direction, List<BakedQuad>> models = new HashMap<>();
        //private final RenderMaterial renderMaterial;

        private TextureInstance(byte[][] pixels, long id) {
            this.pixels = pixels;
            this.id = id;
        }

        private void initializeTexture() {
            this.texture = new DynamicTexture(WIDTH, WIDTH, false);

            for (int y = 0; y < pixels.length && y < WIDTH; y++) {
                for (int x = 0; x < pixels[y].length && x < WIDTH; x++) { //getColoredPixel(BlackboardBlock.colorFromByte(pixels[x][y]),x,y)
                    this.texture.getPixels().setPixelRGBA(x, y, getColoredPixel(pixels[x][y], x, y));
                }
            }
            this.texture.upload();

            this.textureLocation = BlackboardTextureManager.this.textureManager.register("blackboard/" + Long.toHexString(id), this.texture);
            this.renderType = RenderType.entitySolid(textureLocation);
        }

        @Nonnull
        public List<BakedQuad> getModel(Direction dir, Function<byte[][],List<BakedQuad>> modelFactory){
            if(!models.containsKey(dir)){
                this.models.put(dir, modelFactory.apply(pixels));
            }
            return models.get(dir);
        }

        @Nonnull
        public ResourceLocation getTextureLocation() {
            if(textureLocation == null){
                this.initializeTexture();
            }
            return textureLocation;
        }

        @Nonnull
        public RenderType getRenderType() {
            if(renderType == null){
                this.initializeTexture();
            }
            return renderType;
        }

        //should be called when cache expires
        @Override
        public void close() {
            this.texture.close();
        }
    }

    private static int getColoredPixel(byte i, int x, int y) {
        int offset = i > 0 ? 16 : 0;
        int tint = BlackboardBlock.colorFromByte(i);
        TextureAtlas textureMap = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = textureMap.getSprite(Textures.BLACKBOARD_TEXTURE);
        return getTintedColor(sprite, x, y, offset, tint);
    }


    private static int getTintedColor(TextureAtlasSprite sprite, int x, int y, int offset, int tint) {
        if (sprite == null || sprite.getFrameCount() == 0) return -1;
        int tintR = tint >> 16 & 255;
        int tintG = tint >> 8 & 255;
        int tintB = tint & 255;

        int pixel = sprite.getPixelRGBA(0, Math.min(sprite.getWidth()-1, x + offset), Math.min(sprite.getHeight()-1, y));

        // this is in 0xAABBGGRR format, not the usual 0xAARRGGBB.
        int totalB = pixel >> 16 & 255;
        int totalG = pixel >> 8 & 255;
        int totalR = pixel & 255;
        return NativeImage.combine(255, totalB * tintB / 255, totalG * tintG / 255, totalR * tintR / 255);
    }


}

