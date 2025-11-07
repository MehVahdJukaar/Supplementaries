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

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return this.delegate.addVertex(x, y, z);
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this.delegate.setColor(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return this.delegate.setUv(getU(u), getV(v));
    }

    private float getU(float u) {
        float us = this.sprite.getU1() - this.sprite.getU0();
        return (u - this.sprite.getU0()) / us;
    }

    private float getV(float v) {
        float vs = this.sprite.getV1() - this.sprite.getV0();
        return (v - this.sprite.getV0()) / vs;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this.delegate.setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this.delegate.setUv2(u, v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return this.delegate.setNormal(x, y, z);
    }

}
