package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class BombExplosionEmitterParticle extends MetaParticle {
    private int timeSinceStart;
    private final int maximumTime = 8;

    private BombExplosionEmitterParticle(ClientWorld world, double x, double y, double z) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public void tick() {
        for(int i = 0; i < 3; ++i) {
            double d0 = this.posX + (this.rand.nextDouble() - this.rand.nextDouble()) * 2.2D;
            double d1 = this.posY + (this.rand.nextDouble() - this.rand.nextDouble()) * 2.2D;
            double d2 = this.posZ + (this.rand.nextDouble() - this.rand.nextDouble()) * 2.2D;
            this.world.addParticle(Registry.BOMB_EXPLOSION_PARTICLE.get(), d0, d1, d2, (float)this.timeSinceStart / (float)this.maximumTime, 0.0D, 0.0D);
        }

        ++this.timeSinceStart;
        if (this.timeSinceStart == this.maximumTime) {
            this.setExpired();
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new BombExplosionEmitterParticle(worldIn, x, y, z);
        }
    }

}
