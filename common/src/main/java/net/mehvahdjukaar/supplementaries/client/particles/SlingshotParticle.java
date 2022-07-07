package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

public class SlingshotParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    private SlingshotParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(world, x, y, z);
        this.sprites = sprites;

        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;

        this.quadSize *= 2.5F;
        //longer
        this.lifetime = (int) (10.0D / (this.random.nextFloat() * 0.3D + 0.7D));
        this.hasPhysics = false;

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        //crazy hyperbole instead of normal parabula. idk
        float x = this.age / (float) this.lifetime;
        final float a = MthUtils.PHI;
        this.alpha = a + 1 / (x - a);
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