package net.mehvahdjukaar.supplementaries.client.particles;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;
import java.util.function.Supplier;

public class CannonFireParticle extends TextureSheetParticle {
    private final SpriteSet ringSprites;
    private final SpriteSet boomSprites;
    private final double yaw;
    private final double pitch;

    private TextureAtlasSprite boomSprite;

    private CannonFireParticle(ClientLevel world, double x, double y, double z, double pitch, double yaw,
                               SpriteSet ringSprites, SpriteSet boomSprites, float size) {
        super(world, x, y, z, 0, 0, 0);
        this.setParticleSpeed(0, 0, 0);
        this.pitch = pitch;
        this.yaw = yaw;
        this.ringSprites = ringSprites;
        this.boomSprites = boomSprites;
        this.lifetime = 5;
        this.hasPhysics = false;
        this.quadSize = 1.25f * size;

        this.setSpriteFromAge(ringSprites);
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(ringSprites);
    }

    @Override
    public void setSpriteFromAge(SpriteSet sprite) {
        if (!this.removed) {
            // fixes vanilla off by one making last sprite appear for just for 1 frame
            this.setSprite(sprite.get(this.age, this.lifetime));
            this.boomSprite = boomSprites.get(age, lifetime);
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Vec3 vec3 = renderInfo.getPosition();
        float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - vec3.x());
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - vec3.y());
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - vec3.z());
        Quaternionf quaternionf = new Quaternionf();
        quaternionf.rotateY((float) yaw);
        quaternionf.rotateX((float) pitch);

        float scale = this.getQuadSize(partialTicks);

        Matrix4f mat = new Matrix4f();
        mat.translate(px, py, pz);
        mat.scale(scale, scale, scale);
        mat.rotate(quaternionf);

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);

        drawDoubleQuad(buffer, mat, 1, 0, u0, u1, v0, v1, light);

        mat.translate(0, 0, -0.25f);
        mat.rotate(RotHlpr.YN90);

        float U0 = boomSprite.getU0();
        float U1 = boomSprite.getU1();
        float V0 = boomSprite.getV0();
        float V1 = boomSprite.getV1();

        int i = (int) Math.min(4, ((float)age / (lifetime)) * 5 )+1;

        float d = i / 16f;
        float s = 0.25f;


        for (int j = 0; j < 4; j++) {
            mat.rotate(RotHlpr.X90);

            drawDoubleQuad(buffer, mat, s, d, U0, U1, V0, V1, light);
        }
    }

    private void drawDoubleQuad(VertexConsumer buffer, Matrix4f mat, float w, float o, float u0, float u1, float v0,
                                float v1, int light) {

        int lU = VertexUtil.lightU(light);
        int lV = VertexUtil.lightV(light);

        buffer.addVertex(mat, -w, -w, o).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(lU, lV);
        buffer.addVertex(mat, -w, w, o).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(lU, lV);
        buffer.addVertex(mat, w, w, o).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(lU, lV);
        buffer.addVertex(mat, w, -w, o).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(lU, lV);


        // Second quad (mirrored)
        buffer.addVertex(mat, w, -w, o).setUv(u0, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(lU, lV);
        buffer.addVertex(mat, w, w, o).setUv(u0, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(lU, lV);
        buffer.addVertex(mat, -w, w, o).setUv(u1, v0).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(lU, lV);
        buffer.addVertex(mat, -w, -w, o).setUv(u1, v1).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setUv2(lU, lV);

    }

    @Override
    public int getLightColor(float partialTick) {
        if (true) return LightTexture.FULL_BRIGHT;
        float f = ((float) this.age + partialTick) / (float) this.lifetime;
        f = Mth.clamp(f, 0.0F, 1.0F);
        int i = super.getLightColor(partialTick);
        int j = i & 255;
        int k = i >> 16 & 255;
        j += (int) (f * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final Supplier<SpriteSet> sprites2 = Suppliers.memoize(() -> {
            TextureAtlas atlas = ((TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES));
            return new SimpleSpriteSet(List.of(
                    atlas.getSprite(Supplementaries.res("cannon_bang_00")),
                    atlas.getSprite(Supplementaries.res("cannon_bang_01")),
                    atlas.getSprite(Supplementaries.res("cannon_bang_02")),
                    atlas.getSprite(Supplementaries.res("cannon_bang_03")),
                    atlas.getSprite(Supplementaries.res("cannon_bang_04")),
                    atlas.getSprite(Supplementaries.res("empty"))));
        });

        public Factory(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z,
                                       double pitch, double yaw, double size) {
            Vec3 offset = Vec3.directionFromRotation((float) pitch * Mth.RAD_TO_DEG, -(float) yaw * Mth.RAD_TO_DEG);
            offset = offset.scale(-6.501 / 16f);
            offset =  offset.add(0, 1/16f, 0);
            offset =  offset.scale(size);


            return new CannonFireParticle(worldIn, x + offset.x, y + offset.y+1/16f, z + offset.z, pitch, yaw,
                    sprites, sprites2.get(), (float) size);
        }
    }

    record SimpleSpriteSet(List<TextureAtlasSprite> sprites) implements SpriteSet {

        public TextureAtlasSprite get(int age, int lifetime) {
            return this.sprites.get((this.sprites.size() - 1) * age / lifetime);
        }

        public TextureAtlasSprite get(RandomSource random) {
            return this.sprites.get(random.nextInt(this.sprites.size()));
        }
    }

}
