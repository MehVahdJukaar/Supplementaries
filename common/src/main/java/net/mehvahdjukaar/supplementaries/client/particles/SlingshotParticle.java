package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class SlingshotParticle extends TextureSheetParticle {

    private SlingshotParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(world, x, y, z);
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;

        this.quadSize *= 1F;
        //longer
        this.lifetime = (int) (10.0D / (this.random.nextFloat() * 0.3D + 0.7D));
        this.hasPhysics = false;

        this.pickSprite(sprites);
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float d = (this.age+ partialTicks) / (float) this.lifetime ;
        return Mth.lerp(d, this.quadSize, this.quadSize * 5.8f);
    }

    @Override
    public void tick() {
        super.tick();
        //crazy hyperbole instead of normal parabula. idk
        float d = this.age / (float) this.lifetime;
        final float p = MthUtils.PHI;
        this.alpha = p + 1 / (d - p);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Factory(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new SlingshotParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }

}