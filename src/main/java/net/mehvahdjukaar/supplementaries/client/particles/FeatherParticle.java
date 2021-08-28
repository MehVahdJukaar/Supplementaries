package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FeatherParticle extends SpriteTexturedParticle {
    private final float rotSpeed;

    private FeatherParticle(ClientWorld worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double particleRedIn, double particleGreenIn, double particleBlueIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.quadSize *= 0.6F+this.random.nextFloat()*0.2;
        this.lifetime = 60 + this.random.nextInt(20);
        this.rotSpeed = (this.random.nextFloat() - 0.5F) * 0.1F;
        this.roll = this.random.nextFloat() * ((float) Math.PI * 2F);

        this.rCol = (float) particleRedIn;
        this.gCol = (float) particleGreenIn;
        this.bCol = (float) particleBlueIn;

    }

    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.yd -= (double) 0.002F;
            this.yd = Math.max(this.yd, (double) -0.1F);

            this.oRoll = this.roll;
            if (!this.onGround) {
                this.roll += (float) Math.PI * this.rotSpeed * 1.6F;
            } else {
                this.yd = 0.0D;
            }

            if (this.onGround || this.y < 0) {
                this.age++;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite sprite) {
            this.spriteSet = sprite;
        }

        @Override
        public Particle createParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FeatherParticle particle = new FeatherParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.setColor((float) xSpeed, (float) ySpeed, (float) zSpeed);
            particle.pickSprite(this.spriteSet);
            return particle;
        }


    }
}