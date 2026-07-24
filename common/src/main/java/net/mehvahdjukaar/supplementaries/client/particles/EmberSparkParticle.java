package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

// Tiny ember sparks that fly off the cannon wick while the fuse burns.
// Ported from the bedrock razz_sup:ember_spark particle: a small point that shrinks as it dies
// and fades along a white -> yellow -> orange -> red gradient.
public class EmberSparkParticle extends TextureSheetParticle {

    // gradient stops (age fraction -> ARGB), matching the bedrock tinting gradient
    private static final float[] STOPS = {0.0f, 0.08f, 0.22f, 0.4f, 0.58f, 0.72f, 0.86f, 1.0f};
    private static final int[] COLORS = {
            0xFFFFFFFF, 0xFFFFFAC8, 0xFFFFE060, 0xFFFF9E28,
            0xFFFF5E12, 0xFFEA3206, 0x80861C04, 0x00000000
    };

    // bedrock renders the spark at world size 0.14 (full width); Java quadSize is a half-extent
    private static final float BASE_SIZE = 0.04f;
    // full size until this many ticks remain, then shrinks to 0 (bedrock: last 0.2s)
    private static final float SHRINK_TICKS = 4f;

    protected EmberSparkParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                                 SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        // bedrock: linear_acceleration -5 b/s^2 -> yd -= 0.04*gravity per tick; drag 0.9/s -> friction^20 ~= e^-0.9
        this.gravity = 0.3125f;
        this.friction = 0.956f;
        this.quadSize = BASE_SIZE;
        this.lifetime = 11 + this.random.nextInt(11);
        this.setSpriteFromAge(sprites);
        this.updateColor(0);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        int total = super.getLightColor(partialTick);
        int sky = LightTexture.sky(total);
        return LightTexture.pack(Math.max(LightTexture.block(total), 14), sky);
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float remaining = this.lifetime - (this.age + scaleFactor);
        float shrink = Mth.clamp(remaining / SHRINK_TICKS, 0f, 1f);
        return BASE_SIZE * shrink;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.updateColor(1);
        }
    }

    private void updateColor(int ageOffset) {
        float t = Mth.clamp((this.age + ageOffset) / (float) this.lifetime, 0f, 1f);
        int i = 1;
        while (i < STOPS.length - 1 && t > STOPS[i]) i++;
        float local = (t - STOPS[i - 1]) / (STOPS[i] - STOPS[i - 1]);
        int a = COLORS[i - 1], b = COLORS[i];
        this.rCol = lerpChannel(a, b, local, 16);
        this.gCol = lerpChannel(a, b, local, 8);
        this.bCol = lerpChannel(a, b, local, 0);
        this.alpha = lerpChannel(a, b, local, 24);
    }

    private static float lerpChannel(int colorA, int colorB, float t, int shift) {
        float a = ((colorA >> shift) & 0xFF) / 255f;
        float b = ((colorB >> shift) & 0xFF) / 255f;
        return Mth.lerp(t, a, b);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new EmberSparkParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
