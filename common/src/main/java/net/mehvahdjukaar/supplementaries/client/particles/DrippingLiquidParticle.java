package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class DrippingLiquidParticle extends TextureSheetParticle {
    private final Fluid fluid;

    private DrippingLiquidParticle(ClientLevel world, double x, double y, double z, Fluid fluid) {
        super(world, x, y, z);
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.06F;
        this.fluid = fluid;
        this.gravity *= 0.02F;
        this.lifetime = 40;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.ageParticle();
        if (!this.removed) {
            this.yd -=  this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.updateMotion();
            if (!this.removed) {
                this.xd *=  0.98F;
                this.yd *=  0.98F;
                this.zd *=  0.98F;
                BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
                FluidState fluidstate = this.level.getFluidState(blockpos);
                if (fluidstate.getType() == this.fluid && this.y <  ( blockpos.getY() + fluidstate.getHeight(this.level, blockpos))) {
                    this.remove();
                }

            }
        }
    }

    protected void ageParticle() {
        if (this.lifetime-- <= 0) {
            this.remove();
            this.level.addParticle(ModParticles.FALLING_LIQUID.get(), this.x, this.y, this.z, this.rCol, this.gCol, this.bCol);
        }

    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    protected void updateMotion() {
        this.xd *= 0.02D;
        this.yd *= 0.02D;
        this.zd *= 0.02D;
    }


    public static class Factory implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet spriteSet;

        public Factory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double r, double g, double b) {
            DrippingLiquidParticle drippingLiquidParticle = new DrippingLiquidParticle(worldIn, x, y, z, Fluids.WATER);
            drippingLiquidParticle.setColor((float) r, (float) g, (float) b);
            drippingLiquidParticle.pickSprite(this.spriteSet);
            return drippingLiquidParticle;
        }
    }
}
