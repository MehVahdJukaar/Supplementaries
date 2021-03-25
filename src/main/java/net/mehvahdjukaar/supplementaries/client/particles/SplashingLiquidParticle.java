package net.mehvahdjukaar.supplementaries.client.particles;


import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.RainParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;


public class SplashingLiquidParticle extends RainParticle {
    private SplashingLiquidParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        this.gravity = 0.04F;
        if (motionY == 0.0D && (motionX != 0.0D || motionZ != 0.0D)) {
            this.xd = motionX;
            this.yd = 0.1D;
            this.zd = motionZ;
        }
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double r, double g, double b) {
            SplashingLiquidParticle splashparticle = new SplashingLiquidParticle(worldIn, x, y, z, 0, 0, 0);
            splashparticle.setColor((float)r, (float)g, (float)b);
            splashparticle.pickSprite(this.spriteSet);
            return splashparticle;
        }
    }

}