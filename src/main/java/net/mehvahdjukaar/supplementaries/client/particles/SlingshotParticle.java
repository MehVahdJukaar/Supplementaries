package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;

public class SlingshotParticle extends SpriteTexturedParticle {

    private final IAnimatedSprite sprites;

    private SlingshotParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ, IAnimatedSprite sprites) {
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
        float x = this.age / (float) this.lifetime;
        final float a = 1 + (MathHelper.sqrt(5f) - 1f) / 2f;
        this.alpha = a + 1 / (x - a);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements IParticleFactory<BasicParticleType> {
        private final IAnimatedSprite sprite;

        public Factory(IAnimatedSprite spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SlingshotParticle particle = new SlingshotParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
            return particle;
        }
    }

}