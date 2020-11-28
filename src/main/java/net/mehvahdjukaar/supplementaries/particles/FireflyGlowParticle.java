package net.mehvahdjukaar.supplementaries.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class FireflyGlowParticle extends SpriteTexturedParticle {
    protected FireflyGlowParticle(ClientWorld worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        this.particleRed = 255;
        this.particleBlue = 0;
        this.particleGreen = 0;
        // used for hitbox. not used
        // this.setSize(0.01F, 0.01F);
        this.particleScale = 0.125f;
        // not used
        // this.motionX =0.2d;
        // this.motionY =0.2d;
        // this.motionZ =0.2d;
        this.maxAge = 40;
    }

    public float getScale(float partialTicks) {
        float f = ((float) this.age + partialTicks) / (float) this.maxAge;
        return this.particleScale * (1 - f) * f * 4;// (1.0F - f * f * 0.5F);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        // this.prevPosX =this.posX;
        // this.prevPosY =this.posY;
        // this.prevPosZ =this.posZ;
        this.age++;
        if (this.age > this.maxAge) {
            this.setExpired();
        }
    }

    // TODO: add this to the entity clamp this to prevent black particle
    public int getBrightnessForRender(float partialTick) {
        float f = this.getScale(partialTick) / this.particleScale;
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        int i = super.getBrightnessForRender(partialTick);
        int j = (int) (f * 240);
        int k = i >> 16 & 255;
        if (j > 240) {
            j = 240;
        }
        return j | k << 16;
    }
    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;
        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed,
                                     double zSpeed) {
            FireflyGlowParticle op = new FireflyGlowParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            op.selectSpriteRandomly(this.spriteSet);
            op.setColor(1f, 1f, 1f);
            return op;
        }
    }
}