package net.mehvahdjukaar.supplementaries.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteCoordinateUnExpander implements VertexConsumer {
    private final VertexConsumer delegate;
    private final TextureAtlasSprite sprite;

    public SpriteCoordinateUnExpander(VertexConsumer vertexConsumer, TextureAtlasSprite textureAtlasSprite) {
        this.delegate = vertexConsumer;
        this.sprite = textureAtlasSprite;
    }

    public VertexConsumer vertex(double x, double y, double z) {
        return this.delegate.vertex(x, y, z);
    }

    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return this.delegate.color(red, green, blue, alpha);
    }

    public VertexConsumer uv(float u, float v) {
        return this.delegate.uv(getU(u), getV(v));
    }

    private float getU(float u) {
        float us = this.sprite.getU1() - this.sprite.getU0();
        return (u - this.sprite.getU0()) / us;
    }

    private float getV(float v) {
        float vs = this.sprite.getV1() - this.sprite.getV0();
        return (v - this.sprite.getV0()) / vs;
    }

    public VertexConsumer overlayCoords(int u, int v) {
        return this.delegate.overlayCoords(u, v);
    }

    public VertexConsumer uv2(int u, int v) {
        return this.delegate.uv2(u, v);
    }

    public VertexConsumer normal(float x, float y, float z) {
        return this.delegate.normal(x, y, z);
    }

    public void endVertex() {
        this.delegate.endVertex();
    }

    public void defaultColor(int defaultR, int defaultG, int defaultB, int defaultA) {
        this.delegate.defaultColor(defaultR, defaultG, defaultB, defaultA);
    }

    public void unsetDefaultColor() {
        this.delegate.unsetDefaultColor();
    }

    public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float texU, float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
        this.delegate.vertex(x, y, z, red, green, blue, alpha,
                this.getU(texU),
                this.getV(texV),
                overlayUV, lightmapUV, normalX, normalY, normalZ);
    }
}
