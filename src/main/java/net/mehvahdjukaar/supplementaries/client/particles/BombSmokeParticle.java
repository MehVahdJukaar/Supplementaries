package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class BombSmokeParticle extends SpriteTexturedParticle {
    private BombSmokeParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        this.multiplyParticleScaleBy(3.0F);
        this.setSize(0.35F, 0.35F);

        this.maxAge = this.rand.nextInt(35) + 30;

        this.particleGravity = 3.0E-6F;
        this.motionX = motionX;
        this.motionY = motionY + 0.01 + (double)(this.rand.nextFloat() / 2000.0F);
        this.motionZ = motionZ;
    }

    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.age++ < this.maxAge && !(this.particleAlpha <= 0.0F)) {
            this.motionX += this.rand.nextFloat() / 3500.0F * (float)(this.rand.nextBoolean() ? 1 : -1);
            this.motionZ += this.rand.nextFloat() / 3500.0F * (float)(this.rand.nextBoolean() ? 1 : -1);
            this.motionY -= this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
            if (this.age >= this.maxAge - 60 && this.particleAlpha > 0.01F) {
                this.particleAlpha -= 0.015F;
            }

        } else {
            this.setExpired();
        }
    }

    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            BombSmokeParticle particle = new BombSmokeParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.setAlphaF(0.9F);
            particle.selectSpriteRandomly(this.spriteSet);
            return particle;
        }
    }

}