package net.mehvahdjukaar.supplementaries.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;



public class SpeakerSoundParticle extends SpriteTexturedParticle {
    protected SpeakerSoundParticle(ClientWorld worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0, 0, 0);

        this.motionX =0; this.motionY=0; this.motionZ=0;
        this.particleRed = Math.max(0.0F, MathHelper.sin(((float)xSpeedIn + 0.0F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        this.particleGreen = Math.max(0.0F, MathHelper.sin(((float)xSpeedIn + 0.33333334F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        this.particleBlue = Math.max(0.0F, MathHelper.sin(((float)xSpeedIn + 0.6666667F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        this.particleScale *= 1.5F;
        this.maxAge = 10;
    }


    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public float getScale(float scaleFactor) {
        float i = 0.2f;
        return this.particleScale * ((MathHelper.sin((float)Math.PI*3*((float)this.age/this.maxAge)))*i +1f - i/2f);
        //return this.particleScale *(((float)this.age/this.maxAge)*0.25f +1f - 0.25f/2f);
        //return this.particleScale * MathHelper.clamp(((float)this.age + scaleFactor) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.age++ >= this.maxAge) {
            this.setExpired();
        } else {
            this.move(this.motionX, this.motionY, this.motionZ);
            if (this.posY == this.prevPosY) {
                this.motionX *= 1.1D;
                this.motionZ *= 1.1D;
            }

            this.motionX *= 0.66;
            this.motionY *= 0.66;
            this.motionZ *= 0.66;
            if (this.onGround) {
                this.motionX *= 0.7;
                this.motionZ *= 0.7;
            }

        }
    }


    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SpeakerSoundParticle op = new SpeakerSoundParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            op.selectSpriteRandomly(this.spriteSet);
            return op;
        }
    }
}