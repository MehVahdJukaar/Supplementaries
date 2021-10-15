package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class BombExplosionEmitterParticle extends NoRenderParticle {
    private int timeSinceStart;
    private final int maximumTime = 8;

    private double radius;

    private BombExplosionEmitterParticle(ClientLevel world, double x, double y, double z, double radius) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.radius = radius;
    }

    public void tick() {
        for(int i = 0; i < 3 +(radius-2)*3; ++i) {
            double d0 = this.x + (this.random.nextDouble() - this.random.nextDouble()) * radius;
            double d1 = this.y + (this.random.nextDouble() - this.random.nextDouble()) * radius;
            double d2 = this.z + (this.random.nextDouble() - this.random.nextDouble()) * radius;
            this.level.addParticle(ModRegistry.BOMB_EXPLOSION_PARTICLE.get(), d0, d1, d2, (float)this.timeSinceStart / (float)this.maximumTime, 0.0D, 0.0D);
        }

        ++this.timeSinceStart;
        if (this.timeSinceStart == this.maximumTime) {
            this.remove();
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double radius, double ySpeed, double zSpeed) {
            return new BombExplosionEmitterParticle(worldIn, x, y, z, radius);
        }
    }

}
