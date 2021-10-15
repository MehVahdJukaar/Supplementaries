package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;



public class BombExplosionParticle extends TextureSheetParticle {
    private final SpriteSet spriteWithAge;

    private BombExplosionParticle(ClientLevel world, double x, double y, double z, double scale, SpriteSet spriteWithAge) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.lifetime = 5 + this.random.nextInt(4);
        float f = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = f;
        this.gCol = f;
        this.bCol = f;
        this.quadSize = 0.65F * (1.0F - (float)scale * 0.5F);
        this.spriteWithAge = spriteWithAge;
        this.setSpriteFromAge(spriteWithAge);

    }
    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.spriteWithAge);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }


    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;
        public Factory(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed,
                                     double zSpeed) {
            return new BombExplosionParticle(worldIn, x, y, z, xSpeed, this.spriteSet);
        }
    }

}