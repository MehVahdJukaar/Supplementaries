package net.mehvahdjukaar.supplementaries.client.renderers;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.mehvahdjukaar.supplementaries.block.blocks.BlackboardBlock;
import net.mehvahdjukaar.supplementaries.block.tiles.BlackboardBlockTile;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class BlackboardTextureManager {

    public static final ResourceLocation BLACKBOARD_ATLAS = new ResourceLocation(Supplementaries.MOD_ID,"textures/atlas/blackboards.png");

    public static BlackboardTextureManager INSTANCE = null;
    //private final AtlasTexture textureAtlas;
    private final TextureManager textureManager;

    private long lastID = 0;

    //will roll over at some point overriding possible textures. Not really a problem since it's ver unlikely somebody will generate 2^64 blackboards in a session
    public long bindNextID(){
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

    public static void init(TextureManager textureManager){
        INSTANCE = new BlackboardTextureManager(textureManager);
    }

    public BlackboardTextureManager(TextureManager textureManager) {
        this.textureManager = textureManager;
        //this.textureAtlas = new AtlasTexture(BLACKBOARD_ATLAS);
        //this.textureManager.register(this.textureAtlas.location(), this.textureAtlas);
    }


    public RenderType getRenderType(BlackboardBlockTile tile){
        return this.getTextureInstance(tile).renderType;
    }

    public RenderType getRenderType(long[] packed){
        return this.getTextureInstance(packed).renderType;
    }

    public ResourceLocation getResourceLocation(long[] packed){
        return getTextureInstance(packed).resourceLocation;
    }

    public ResourceLocation getResourceLocation(BlackboardBlockTile tile){
        return this.getTextureInstance(tile).resourceLocation;
    }

    public ResourceLocation getResoucelocation(BlackboardKey key){
        return this.getTextureInstance(key).resourceLocation;
    }

    public RenderType getRenderType(BlackboardKey key){
        return this.getTextureInstance(key).renderType;
    }

    public TextureInstance getTextureInstance(BlackboardKey key){
        TextureInstance textureInstance = this.blackboardTextures.getIfPresent(key);
        if (textureInstance == null) {
            textureInstance = new TextureInstance(BlackboardBlockTile.unpackPixels(key.values),bindNextID());
            this.blackboardTextures.put(key, textureInstance);
        }
        return textureInstance;
    }

    public TextureInstance getTextureInstance(long[] packed){
        BlackboardKey key = new BlackboardKey(packed);
        TextureInstance textureInstance = this.blackboardTextures.getIfPresent(key);
        if (textureInstance == null) {
            textureInstance = new TextureInstance(BlackboardBlockTile.unpackPixels(packed),bindNextID());
            this.blackboardTextures.put(key, textureInstance);
        }
        return textureInstance;
    }

    private TextureInstance getTextureInstance(BlackboardBlockTile tile) {
        BlackboardKey key = getOrCreateTextureKey(tile);
        TextureInstance textureInstance = this.blackboardTextures.getIfPresent(key);
        if (textureInstance == null) {
            textureInstance = new TextureInstance(tile.pixels,bindNextID());
            this.blackboardTextures.put(key, textureInstance);
        }
        return textureInstance;
    }

    private BlackboardKey getOrCreateTextureKey(BlackboardBlockTile tile){
        if(tile.textureKey==null){
            tile.textureKey = new BlackboardKey(tile.pixels);
        }
        return tile.textureKey;
    }


    public BlackboardKey getUpdatedKey(BlackboardBlockTile tile){
        BlackboardKey key = new BlackboardKey(tile.pixels);
        if(this.blackboardTextures.getIfPresent(key)==null){
            this.blackboardTextures.put(key, new TextureInstance(tile.pixels,bindNextID()));
        }
        return key;
    }

    public static class BlackboardKey{
        private final long[] values;

        public BlackboardKey(long[] packed){
            values = packed;
        }

        public BlackboardKey(byte[][] pixels){
            values = BlackboardBlockTile.packPixels(pixels);;
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


    private class TextureInstance implements AutoCloseable {
        private final DynamicTexture texture;
        private final RenderType renderType;
        private final ResourceLocation resourceLocation;
        //private final RenderMaterial renderMaterial;

        private TextureInstance(byte[][] pixels, long id) {
            this.texture = new DynamicTexture(16, 16, false);
            this.updateTexture(pixels);
            resourceLocation = BlackboardTextureManager.this.textureManager.register("blackboard/" + Long.toHexString(id), this.texture);
            this.renderType = RenderType.entitySolid(resourceLocation);
            //this.renderMaterial = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS,resourceLocation);
        }

        private void updateTexture(byte[][] pixels) {
            for(int y = 0; y< pixels.length; y++) {
                for(int x = 0; x< pixels[y].length; x++) { //getColoredPixel(BlackboardBlock.colorFromByte(pixels[x][y]),x,y)
                    this.texture.getPixels().setPixelRGBA(x, y , getColoredPixel(pixels[x][y],x,y) );
                }
            }
            this.texture.upload();
        }

        //should be called when cache expires
        @Override
        public void close() {
            this.texture.close();
        }
    }

    private static int getColoredPixel(byte i, int x, int y){
        int offset = i > 0?16:0;
        int tint = BlackboardBlock.colorFromByte(i);
        AtlasTexture textureMap = Minecraft.getInstance().getModelManager().getAtlas(AtlasTexture.LOCATION_BLOCKS);
        TextureAtlasSprite sprite = textureMap.getSprite(Textures.BLACKBOARD_TEXTURE);
        return getTintedColor(sprite,x,y,offset,tint);
    }


    private static int getTintedColor(TextureAtlasSprite sprite, int x, int y, int offset, int tint) {
        if (sprite == null || sprite.getFrameCount() == 0) return -1;
        int tintR = tint >> 16 & 255;
        int tintG = tint >> 8 & 255;
        int tintB = tint & 255;

        int pixel = sprite.getPixelRGBA(0, x+offset, y);

        // this is in 0xAABBGGRR format, not the usual 0xAARRGGBB.
        int totalB = pixel >> 16 & 255;
        int totalG = pixel >> 8 & 255;
        int totalR = pixel & 255;
        return NativeImage.combine(255,totalB * tintB / 255 , totalG * tintG / 255, totalR * tintR / 255);
    }


}

