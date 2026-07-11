package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

// meta particle: keeps puffing suds for a few ticks in a given direction (used by the bubble blower dispenser behavior)
public class SudsEmitterParticle extends NoRenderParticle {

    private final Vec3 direction;
    private int timeSinceStart;

    private SudsEmitterParticle(ClientLevel world, double x, double y, double z,
                                double dx, double dy, double dz) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        Vec3 dir = new Vec3(dx, dy, dz);
        double len = dir.length();
        this.direction = len < 1.0E-4 ? new Vec3(0, 0, 1) : dir.scale(1 / len);
        this.lifetime = 4;
    }

    @Override
    public void tick() {
        RandomSource r = this.random;
        Vec3 v = this.direction;
        for (int i = 0; i < 3; i++) {
            double sx = this.x + v.x * 0.5;
            double sy = this.y + v.y * 0.5;
            double sz = this.z + v.z * 0.5;
            Vec3 speed = v.scale(0.1 + r.nextFloat() * 0.1f);
            double vx = speed.x + (0.5 - r.nextFloat()) * 0.08;
            double vy = speed.y + (0.5 - r.nextFloat()) * 0.08;
            double vz = speed.z + (0.5 - r.nextFloat()) * 0.08;
            this.level.addParticle(ModParticles.SUDS_PARTICLE.get(), sx, sy, sz, vx, vy, vz);
        }
        if (this.timeSinceStart++ >= this.lifetime) {
            this.remove();
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        public Factory(SpriteSet sprite) {
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new SudsEmitterParticle(world, x, y, z, dx, dy, dz);
        }
    }
}