package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.common.CommonUtil;
import net.mehvahdjukaar.supplementaries.configs.ClientConfigs;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

import java.util.Random;



public class FireflyGlowParticle extends TextureSheetParticle {
    protected FireflyGlowParticle(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
        //this.particleRed = 1;
        //this.particleBlue = 1;
        //this.particleGreen = 1;
        // used for hitbox. not used
        // this.setSize(0.01F, 0.01F);
        this.quadSize = (float) ClientConfigs.cached.FIREFLY_PAR_SCALE;//0.125f;
        // not used
        // this.motionX =0.2d;
        // this.motionY =0.2d;
        // this.motionZ =0.2d;


        this.lifetime = new Random().nextInt(10)+ ClientConfigs.cached.FIREFLY_PAR_MAXAGE;
        //this.setColor(CommonUtil.ishalloween?0.7f:1,0,1);
    }
    @Override
    public float getQuadSize(float partialTicks) {
        if(CommonUtil.FESTIVITY.isHalloween()){
            this.rCol=0.3f;
            this.gCol=0;
        }
        float f = ((float) this.age + partialTicks) / (float) this.lifetime;
        return this.quadSize * (1 - f) * f * 4;// (1.0F - f * f * 0.5F);
    }



    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        // this.prevPosX =this.posX;
        // this.prevPosY =this.posY;
        // this.prevPosZ =this.posZ;
        this.age++;
        if (this.age > this.lifetime-2) {
            this.remove();
        }
    }

    public int getLightColor(float partialTick) {
        float f = this.getQuadSize(partialTick) / this.quadSize;
        f = Mth.clamp(f, 0.0F, 1.0F);
        this.alpha = f;
        int i = super.getLightColor(partialTick);
        int j = (int) (f * 240);
        int k = i >> 16 & 255;
        j = Math.max(i>>0 & 255, j);
        return j | k << 16;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;
        public Factory(SpriteSet sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed,
                                     double zSpeed) {
            FireflyGlowParticle op = new FireflyGlowParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            op.pickSprite(this.spriteSet);
            op.setColor(1f, 1f, 1f);
            return op;
        }
    }
}