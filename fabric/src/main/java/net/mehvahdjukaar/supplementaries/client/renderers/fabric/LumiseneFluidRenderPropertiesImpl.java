package net.mehvahdjukaar.supplementaries.client.renderers.fabric;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.mehvahdjukaar.supplementaries.client.LumiseneFluidRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class LumiseneFluidRenderPropertiesImpl extends LumiseneFluidRenderProperties implements FluidRenderHandler {

    private TextureAtlasSprite[] single;
    private TextureAtlasSprite[][][] multiple;

    @Override
    public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
        if (pos == null) return single;
        int x = Math.floorMod(pos.getX(), 4);
        int y = Math.floorMod(pos.getZ(), 4);
        return multiple[y][x];

    }

    @Override
    public void reloadTextures(TextureAtlas textureAtlas) {
        single = new TextureAtlasSprite[]{
                textureAtlas.getSprite(SINGLE_STILL_TEXTURE),
                textureAtlas.getSprite(SINGLE_STILL_TEXTURE)
        };
        multiple = new TextureAtlasSprite[4][][];
        for (int x = 0; x < 4; x++) {
            multiple[x] = new TextureAtlasSprite[4][];
            for (int y = 0; y < 4; y++) {
                multiple[x][y] = new TextureAtlasSprite[]{
                        textureAtlas.getSprite(STILL_TEXTURES[x][y]),
                        textureAtlas.getSprite(STILL_TEXTURES[x][y])
                };
            }
        }
    }
}
