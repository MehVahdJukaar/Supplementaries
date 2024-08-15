package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

public class SparkleParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected SparkleParticle(ClientLevel world, double x, double y, double z, double xd, double yd, double zd,
                              SpriteSet sprites) {
        super(world, x, y, z);
        this.sprites = sprites;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.x = x;
        this.y = y;
        this.z = z;
        this.quadSize = 0.2f;
        //this.setColor(0.9f + this.random.nextFloat() * 0.1f,
        //        0.75f + this.random.nextFloat() * 0.15f,
        //        0.6f + this.random.nextFloat() * 0.4F);

        this.lifetime = this.random.nextInt(9) + 4;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    protected int getLightColor(float partialTick) {
        int total = super.getLightColor(partialTick);
        int block = LightTexture.block(total);
        int sky = LightTexture.sky(total);
        return LightTexture.pack(Math.max(block, 11),sky);
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        if (true) return quadSize;
        float f = (this.age + scaleFactor) / this.lifetime;
        f = 1.0F - f;
        f = f * f;
        f = 1.0F - f;
        return this.quadSize * f;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SparkleParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
