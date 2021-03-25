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
        this.gravity = 0.06F;
        this.fluid = fluid;
        this.gravity *= 0.02F;
        this.lifetime = 40;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.ageParticle();
        if (!this.removed) {
            this.yd -= (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.updateMotion();
            if (!this.removed) {
                this.xd *= (double)0.98F;
                this.yd *= (double)0.98F;
                this.zd *= (double)0.98F;
                BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
                FluidState fluidstate = this.level.getFluidState(blockpos);
                if (fluidstate.getType() == this.fluid && this.y < (double)((float)blockpos.getY() + fluidstate.getHeight(this.level, blockpos))) {
                    this.remove();
                }

            }
        }
    }

    protected void ageParticle() {
        if (this.lifetime-- <= 0) {
            this.remove();
            this.level.addParticle(Registry.FALLING_LIQUID.get(), this.x, this.y, this.z, this.rCol, this.gCol, this.bCol);
        }

    }
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    protected void updateMotion() {
        this.xd *= 0.02D;
        this.yd *= 0.02D;
        this.zd *= 0.02D;
    }



    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        protected final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        public Particle createParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double r, double g, double b) {
            DrippingLiquidParticle dripparticle = new DrippingLiquidParticle(worldIn, x, y, z, Fluids.WATER);
            dripparticle.setColor((float)r, (float)g, (float)b);
            dripparticle.pickSprite(this.spriteSet);
            return dripparticle;
        }
    }
}
