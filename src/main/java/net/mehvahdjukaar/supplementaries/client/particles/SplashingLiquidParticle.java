package net.mehvahdjukaar.supplementaries.client.particles;


import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.WaterDropParticle;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;


public class SplashingLiquidParticle extends WaterDropParticle {
    private SplashingLiquidParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        this.gravity = 0.04F;
        if (motionY == 0.0D && (motionX != 0.0D || motionZ != 0.0D)) {
            this.xd = motionX;
            this.yd = 0.1D;
            this.zd = motionZ;
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double r, double g, double b) {
            SplashingLiquidParticle splashparticle = new SplashingLiquidParticle(worldIn, x, y, z, 0, 0, 0);
            splashparticle.setColor((float)r, (float)g, (float)b);
            splashparticle.pickSprite(this.spriteSet);
            return splashparticle;
        }
    }

}