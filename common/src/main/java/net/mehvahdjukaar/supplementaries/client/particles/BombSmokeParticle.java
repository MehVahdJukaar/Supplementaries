package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;


public class BombSmokeParticle extends TextureSheetParticle {
    private BombSmokeParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        this.scale(3.0F);
        this.setSize(0.35F, 0.35F);

        this.lifetime = this.random.nextInt(30) + 20;

        this.gravity = 3.0E-6F;
        this.xd = motionX;
        this.yd = motionY + 0.01 + (this.random.nextFloat() / 2000.0F);
        this.zd = motionZ;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ < this.lifetime && this.alpha > 0.0F) {
            this.xd += this.random.nextFloat() / 3500.0F * (this.random.nextBoolean() ? 1 : -1);
            this.zd += this.random.nextFloat() / 3500.0F * (this.random.nextBoolean() ? 1 : -1);
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);
            if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
                this.alpha -= 0.015F;
            }

        } else {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BombSmokeParticle particle = new BombSmokeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.setAlpha(0.9F);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }

}