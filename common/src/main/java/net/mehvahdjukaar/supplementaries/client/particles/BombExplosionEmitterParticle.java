package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;


public class BombExplosionEmitterParticle extends NoRenderParticle {
    private static final int MAXIMUM_TIME = 8;

    private final double radius;

    private BombExplosionEmitterParticle(ClientLevel world, double x, double y, double z, double radius) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.radius = radius;
    }

    @Override
    public void tick() {
        for (int i = 0; i < 3 + (radius - 2) * 3; ++i) {
            double d0 = this.x + (this.random.nextDouble() - this.random.nextDouble()) * radius;
            double d1 = this.y + (this.random.nextDouble() - this.random.nextDouble()) * radius;
            double d2 = this.z + (this.random.nextDouble() - this.random.nextDouble()) * radius;
            this.level.addParticle(ModParticles.BOMB_EXPLOSION_PARTICLE.get(), d0, d1, d2, (float) this.age / (float) MAXIMUM_TIME, 0.0D, 0.0D);
        }

        ++this.age;
        if (this.age >= BombExplosionEmitterParticle.MAXIMUM_TIME) {
            this.remove();
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        public Factory(SpriteSet sprite) {
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double radius, double ySpeed, double zSpeed) {
            return new BombExplosionEmitterParticle(worldIn, x, y, z, radius);
        }
    }

}
