package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;



public class BombExplosionParticle extends SpriteTexturedParticle {
    private final IAnimatedSprite spriteWithAge;

    private BombExplosionParticle(ClientWorld world, double x, double y, double z, double scale, IAnimatedSprite spriteWithAge) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.maxAge = 5 + this.rand.nextInt(4);
        float f = this.rand.nextFloat() * 0.6F + 0.4F;
        this.particleRed = f;
        this.particleGreen = f;
        this.particleBlue = f;
        this.particleScale = 0.65F * (1.0F - (float)scale * 0.5F);
        this.spriteWithAge = spriteWithAge;
        this.selectSpriteWithAge(spriteWithAge);

    }

    public int getBrightnessForRender(float partialTick) {
        return 15728880;
    }

    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.age++ >= this.maxAge) {
            this.setExpired();
        } else {
            this.selectSpriteWithAge(this.spriteWithAge);
        }
    }

    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_LIT;
    }



    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;
        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed,
                                     double zSpeed) {
            return new BombExplosionParticle(worldIn, x, y, z, xSpeed, this.spriteSet);
        }
    }

}