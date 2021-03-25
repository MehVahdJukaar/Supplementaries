package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;



public class SpeakerSoundParticle extends SpriteTexturedParticle {
    protected SpeakerSoundParticle(ClientWorld worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0, 0, 0);

        this.xd =0; this.yd=0; this.zd=0;
        this.rCol = Math.max(0.0F, MathHelper.sin(((float)xSpeedIn + 0.0F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        this.gCol = Math.max(0.0F, MathHelper.sin(((float)xSpeedIn + 0.33333334F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        this.bCol = Math.max(0.0F, MathHelper.sin(((float)xSpeedIn + 0.6666667F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        this.quadSize *= 1.5F;
        this.lifetime = 10;
    }


    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public float getQuadSize(float scaleFactor) {
        float i = 0.2f;
        return this.quadSize * ((MathHelper.sin((float)Math.PI*3*((float)this.age/this.lifetime)))*i +1f - i/2f);
        //return this.particleScale *(((float)this.age/this.maxAge)*0.25f +1f - 0.25f/2f);
        //return this.particleScale * MathHelper.clamp(((float)this.age + scaleFactor) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            if (this.y == this.yo) {
                this.xd *= 1.1D;
                this.zd *= 1.1D;
            }

            this.xd *= 0.66;
            this.yd *= 0.66;
            this.zd *= 0.66;
            if (this.onGround) {
                this.xd *= 0.7;
                this.zd *= 0.7;
            }

        }
    }


    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SpeakerSoundParticle op = new SpeakerSoundParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            op.pickSprite(this.spriteSet);
            return op;
        }
    }
}