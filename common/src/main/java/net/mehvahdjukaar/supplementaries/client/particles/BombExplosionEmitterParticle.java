package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;


public class BombExplosionEmitterParticle extends NoRenderParticle {
    private static final int MAXIMUM_TIME = 4;
    // the blast eats blocks well past its nominal radius, so the cloud has to reach out there too
    private static final double SPREAD = 1.6;
    private static final double DENSITY = 1.5;
    private static final int MIN_PARTICLES = 5;
    private static final int MAX_PARTICLES = 150;

    private final double radius;

    private BombExplosionEmitterParticle(ClientLevel world, double x, double y, double z, double radius) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.radius = radius;
    }

    @Override
    public void tick() {
        // scale with volume
        int count = Mth.clamp(Mth.ceil(radius * radius * radius * DENSITY), MIN_PARTICLES, MAX_PARTICLES);
        for (int i = 0; i < count; ++i) {

            double phi = Math.acos(2 * random.nextDouble() - 1);// Inverse of cumulative distribution function for uniform distribution in [0, π]
            double theta = random.nextDouble() * 2 * Math.PI;

            // cube root spreads them evenly through the ball. A plain random clumps them all in the middle
            double r = radius * SPREAD * Math.cbrt(random.nextDouble());

            double d0 = this.x + r * Math.sin(phi) * Math.cos(theta);
            double d1 = this.y + r * Math.sin(phi) * Math.sin(theta);
            double d2 = this.z + r * Math.cos(phi);
            this.level.addParticle(ModParticles.BOMB_EXPLOSION_PARTICLE.get(), d0, d1, d2,
                    (float) this.age / (float) MAXIMUM_TIME, 0.0D, 0.0D);
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
            if (radius <= 0) return null;
            return new BombExplosionEmitterParticle(worldIn, x, y, z, radius);
        }
    }

}
