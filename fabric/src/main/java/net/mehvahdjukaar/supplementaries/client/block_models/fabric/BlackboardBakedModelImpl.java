package net.mehvahdjukaar.supplementaries.client.block_models.fabric;

import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.mehvahdjukaar.supplementaries.common.Textures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class BlackboardBakedModelImpl {
    public static BakedQuad createPixelQuad(float x, float y, float z, float width, float height,
                                            TextureAtlasSprite sprite, int color, Transformation transform) {
        Vector3f normal = new Vector3f(0, 0, -1);
        applyModelRotation(normal, transform);
        Direction dir = Direction.getNearest(normal.x(), normal.y(), normal.z());

        float tu = sprite.getWidth() * width;
        float tv = sprite.getHeight() * height;
        float u0 = x * 16;
        float v0 = y * 16;

        sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(Textures.TIMBER_CROSS_BRACE_TEXTURE);

        MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
        QuadEmitter emitter = meshBuilder.getEmitter();

        emitter.nominalFace(dir);

        putVertex(0,emitter, normal, x + width, y + height, z,
                u0 + tu, v0 + tv, sprite, color, transform);
        putVertex(1,emitter, normal, x + width, y, z,
                u0 + tu, v0, sprite, color, transform);
        putVertex(2,emitter, normal, x, y, z,
                u0, v0, sprite, color, transform);
        putVertex(3,emitter, normal, x, y + height, z,
                u0, v0 + tv, sprite, color, transform);


        emitter.sprite(0, 0, sprite.getU(u0), sprite.getV(v0));
        emitter.sprite(1, 0, sprite.getU(u0), sprite.getV(v0 + tv));
        emitter.sprite(2, 0, sprite.getU(u0 + tu), sprite.getV(v0 + tv));
        emitter.sprite(3, 0, sprite.getU(u0 + tu), sprite.getV(v0));
        emitter.square(dir, x, y, x + width, y + height, z);

        emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);

        //  emitter.spriteBake(0, sprite, MutableQuadView.BAKE_NORMALIZED);
        emitter.spriteColor(0, 0x232323, 0xff2244ff, 0xff222255, 0x22442255);

        return emitter.toBakedQuad(0, sprite, false);
    }

    private static void putVertex(int vertInd, QuadEmitter builder, Vector3f normal,
                                  float x, float y, float z, float u, float v,
                                  TextureAtlasSprite sprite, int color, Transformation transformation) {

        Vector3f posV = new Vector3f(x, y, z);
        applyModelRotation(posV, transformation);

        builder.pos(vertInd, posV.x(), posV.y(), posV.z());
        builder.spriteColor(vertInd, 0, color);
        builder.sprite(vertInd, 0, sprite.getU(u), sprite.getV(v));
        builder.normal(0, normal.x(), normal.y(), normal.z());
    }


    public static void applyModelRotation(Vector3f pPos, Transformation pTransform) {
        if (pTransform != Transformation.identity()) {
            rotateVertexBy(pPos, new Vector3f(0.5F, 0.5F, 0.5F), pTransform.getMatrix());
        }
    }

    private static void rotateVertexBy(Vector3f pPos, Vector3f pOrigin, Matrix4f pTransform) {
        Vector4f vector4f = new Vector4f(pPos.x() - pOrigin.x(), pPos.y() - pOrigin.y(), pPos.z() - pOrigin.z(), 1.0F);
        vector4f.transform(pTransform);
        pPos.set(vector4f.x() + pOrigin.x(), vector4f.y() + pOrigin.y(), vector4f.z() + pOrigin.z());
    }

}
