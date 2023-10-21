package net.mehvahdjukaar.supplementaries.client.renderers.fabric;

import com.google.common.base.Preconditions;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector4f;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.mehvahdjukaar.supplementaries.client.renderers.BakedQuadBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.Consumer;

public class BakedQuadBuilderImpl implements BakedQuadBuilder {


    public static BakedQuadBuilder create(TextureAtlasSprite sprite, @Nullable Matrix4f transformation) {
        return new BakedQuadBuilderImpl(sprite, transformation);
    }

    private final QuadEmitter inner;
    private final TextureAtlasSprite sprite;
    private final Matrix4f globalTransform;
    private final Matrix3f normalTransf;
    private Consumer<BakedQuad> quadConsumer;
    private int vertexIndex = 0;
    private boolean autoDirection = false;

    private BakedQuadBuilderImpl(TextureAtlasSprite sprite, @Nullable Matrix4f transform) {
        MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        this.inner = meshBuilder.getEmitter();
        this.globalTransform = transform; //new Matrix4f(new Matrix3f(transform));
        this.sprite = sprite;
        inner.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
        this.normalTransf = transform == null ? null :
                new Matrix3f(transform).invert().transpose(); //forge uses this in quad transform. idk how it works
    }

    @Override
    public BakedQuadBuilder setAutoBuild(Consumer<BakedQuad> quadConsumer) {
        this.quadConsumer = quadConsumer;
        return this;
    }

    @Override
    public BakedQuadBuilder setTint(int tintIndex) {
        inner.colorIndex(tintIndex);
        return this;
    }

    @Override
    public BakedQuadBuilderImpl setAutoDirection() {
        this.autoDirection = true;
        return this;
    }

    @Override
    public BakedQuadBuilderImpl setShade(boolean shade) {
        //cant do this on fabric
        return this;
    }

    @Override
    public BakedQuadBuilderImpl setAmbientOcclusion(boolean ambientOcclusion) {
        return this;
    }

    public BakedQuadBuilderImpl setDirection(Direction direction) {
        if (globalTransform != null) {
            direction = Direction.rotate(globalTransform, direction);
        }
        inner.nominalFace(direction);
        return this;
    }


    @Override
    public BakedQuadBuilderImpl vertex(double x, double y, double z) {
        if (globalTransform != null) {
            Vector4f v = globalTransform.transform(new Vector4f((float) x, (float) y, (float) z, 1.0F));
            inner.pos(vertexIndex, v.x(), v.y(), v.z());
            return this;
        }
        inner.pos(vertexIndex, (float) x, (float) y, (float) z);
        return this;
    }


    @Override
    public BakedQuadBuilderImpl normal(float x, float y, float z) {
        if (globalTransform != null) {
            Vector3f normal = normalTransf.transform(new Vector3f(x, y, z));
            normal.normalize();
            inner.normal(vertexIndex, normal.x(), normal.y(), normal.z());
        } else inner.normal(vertexIndex, x, y, z);
        if (autoDirection) {
            this.setDirection(Direction.getNearest(x, y, z));
        }
        return this;
    }

    @Override
    public BakedQuadBuilderImpl color(int rgba) {
        inner.color(vertexIndex, rgba);
        return this;
    }

    @Override
    public BakedQuadBuilderImpl color(int r, int g, int b, int a) {
        return color(((a & 0xFF) << 24) |
                ((b & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (r & 0xFF));
    }

    @Override
    public BakedQuadBuilderImpl uv(float u, float v) {
        inner.uv(vertexIndex, sprite.getU(u * 16), sprite.getV(v * 16));
        return this;
    }

    @Override
    public BakedQuadBuilderImpl overlayCoords(int u, int v) {
        return this;
    }

    @Override
    public BakedQuadBuilderImpl uv2(int u, int v) {
        inner.lightmap(vertexIndex, (u & 0xFFFF) | ((v & 0xFFFF) << 16));
        return this;
    }

    @Override
    public BakedQuadBuilderImpl lightEmission(int lightLevel) {
        inner.material(RendererAccess.INSTANCE.getRenderer().materialFinder().emissive(true).find());
        return this;
    }

    @Override
    public void endVertex() {
        vertexIndex++;
        if (vertexIndex == 4) {
            vertexIndex = 0;
            if (quadConsumer != null) {
                quadConsumer.accept(this.build());
            }
        }
    }

    @Override
    public void defaultColor(int defaultR, int defaultG, int defaultB, int defaultA) {
    }

    @Override
    public void unsetDefaultColor() {
    }

    @Override
    public BakedQuadBuilder fromVanilla(BakedQuad quad) {
        inner.fromVanilla(quad, RendererAccess.INSTANCE.getRenderer().materialFinder().find(), null);
        return null;
    }

    @Override
    public BakedQuad build() {
        Preconditions.checkNotNull(sprite, "sprite cannot be null");
        return inner.toBakedQuad(sprite);
    }
}

