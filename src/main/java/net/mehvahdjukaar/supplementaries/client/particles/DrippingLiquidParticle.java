package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.setup.Registry;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DrippingLiquidParticle extends SpriteTexturedParticle{
    private final Fluid fluid;
    private DrippingLiquidParticle(ClientWorld world, double x, double y, double z, Fluid fluid) {
        super(world, x, y, z);
        this.setSize(0.01F, 0.01F);
        this.particleGravity = 0.06F;
        this.fluid = fluid;
        this.particleGravity *= 0.02F;
        this.maxAge = 40;
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
                this.motionX *= (double)0.98F;
                this.motionY *= (double)0.98F;
                this.motionZ *= (double)0.98F;
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
            this.world.addParticle(Registry.FALLING_LIQUID.get(), this.posX, this.posY, this.posZ, this.particleRed, this.particleGreen, this.particleBlue);
        }

    }
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    protected void updateMotion() {
        this.motionX *= 0.02D;
        this.motionY *= 0.02D;
        this.motionZ *= 0.02D;
    }



    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        protected final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double r, double g, double b) {
            DrippingLiquidParticle dripparticle = new DrippingLiquidParticle(worldIn, x, y, z, Fluids.WATER);
            dripparticle.setColor((float)r, (float)g, (float)b);
            dripparticle.selectSpriteRandomly(this.spriteSet);
            return dripparticle;
        }
    }
}
