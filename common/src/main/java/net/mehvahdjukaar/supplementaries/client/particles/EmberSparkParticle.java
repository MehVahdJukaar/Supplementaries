package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class EmberSparkParticle extends TextureSheetParticle {

    private static final int HOT_COLOR = 0xFFFAC8;
    private static final int COLD_COLOR = 0x8E1C04;

    private static final float BASE_SIZE = 0.04f;
    private static final float FADE_TICKS = 4;

    protected EmberSparkParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                                 SpriteSet sprites) {
        super(level, x, y, z);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.gravity = 0.3125f;
        this.friction = 0.956f;
        this.quadSize = BASE_SIZE;
        this.lifetime = 11 + this.random.nextInt(11);
        this.setSpriteFromAge(sprites);
        this.updateColor();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        int total = super.getLightColor(partialTick);
        return LightTexture.pack(Math.max(LightTexture.block(total), 14), LightTexture.sky(total));
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return BASE_SIZE * this.getFade(scaleFactor);
    }

    @Override
    public void tick() {
        super.tick();
        this.updateColor();
        this.alpha = this.getFade(0);
    }

    private float getFade(float partialTick) {
        float remaining = this.lifetime - (this.age + partialTick);
        return Mth.clamp(remaining / FADE_TICKS, 0, 1);
    }

    private void updateColor() {
        //sqrt so it loses the white hot look almost immediately
        float t = Mth.sqrt(this.age / (float) this.lifetime);
        int c = FastColor.ARGB32.lerp(t, HOT_COLOR, COLD_COLOR);
        this.setColor(FastColor.ARGB32.red(c) / 255f, FastColor.ARGB32.green(c) / 255f, FastColor.ARGB32.blue(c) / 255f);
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
