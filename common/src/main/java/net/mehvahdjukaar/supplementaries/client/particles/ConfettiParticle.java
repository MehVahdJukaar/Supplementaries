package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;

public class ConfettiParticle extends TextureSheetParticle {

    private ConfettiParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ, SpriteSet sprites) {
        super(world, x, y, z);

        this.pickSprite(sprites);

        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;

        this.setSize(0.001F, 0.001F);
        this.gravity = 0.015F / 0.04F;
        //longer
        this.lifetime = (int) (80.0D / (this.random.nextFloat() * 0.3D + 0.7D));

        int col = ColorHelper.getRandomBrightColor(this.random);
        /*
        float i = random.nextFloat();
        this.rCol = Math.max(0.0F, MathHelper.sin((i + 0.0F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        this.gCol = Math.max(0.0F, MathHelper.sin((i + 0.33333334F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        this.bCol = Math.max(0.0F, MathHelper.sin((i + 0.6666667F) * ((float)Math.PI * 2F)) * 0.65F + 0.35F);
        */

        this.rCol = FastColor.ARGB32.red(col) / 255f;
        this.gCol = FastColor.ARGB32.green(col) / 255f;
        this.bCol = FastColor.ARGB32.blue(col) / 255f;
    }

    @Override
    public void tick() {
        super.tick();
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

            return new ConfettiParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }

}