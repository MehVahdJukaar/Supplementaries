package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.BlockPos;


public class FallingLiquidParticle extends SpriteTexturedParticle {
    private final Fluid fluid;

    private FallingLiquidParticle(ClientWorld world, double x, double y, double z, Fluid fluid) {
        super(world, x, y, z);
        this.setSize(0.01F, 0.01F);
        this.particleGravity = 0.06F;
        this.fluid = fluid;
        this.maxAge = (int)(64.0D / (Math.random() * 0.8D + 0.2D));
    }

    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public void tick() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.ageParticle();
        if (!this.isExpired) {
            this.motionY -= (double)this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
            this.updateMotion();
            if (!this.isExpired) {
                this.motionX *= 0.98F;
                this.motionY *= 0.98F;
                this.motionZ *= 0.98F;
                BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
                FluidState fluidstate = this.world.getFluidState(blockpos);
                if (fluidstate.getFluid() == this.fluid && this.posY < (double)((float)blockpos.getY() + fluidstate.getActualHeight(this.world, blockpos))) {
                    this.setExpired();
                }
            }
        }
    }

    protected void ageParticle() {
        if (this.maxAge-- <= 0) {
            this.setExpired();
        }
    }

    protected void updateMotion() {
        if (this.onGround) {
            this.setExpired();
            this.world.addParticle(Registry.SPLASHING_LIQUID.get(), this.posX, this.posY, this.posZ, this.particleRed, this.particleGreen, this.particleBlue);
        }
    }



    public static class Factory implements IParticleFactory<BasicParticleType> {
        protected final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double r, double g, double b) {
            FallingLiquidParticle fallingparticle = new FallingLiquidParticle(worldIn, x, y, z, Fluids.WATER);
            fallingparticle.setColor((float)r, (float)g, (float)b);
            fallingparticle.selectSpriteRandomly(this.spriteSet);
            return fallingparticle;
        }
    }

}