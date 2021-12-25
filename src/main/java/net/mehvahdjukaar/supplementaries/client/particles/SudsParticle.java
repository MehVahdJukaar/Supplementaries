package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.Random;

public class SudsParticle extends BubbleBlockParticle {

    private final double additionalSize;

    SudsParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ,pXSpeed, pYSpeed, pZSpeed, pSprites);
        this.friction = 0.96F;
        this.gravity = -0.05F;
        this.speedUpWhenYMotionIsBlocked = true;

        this.additionalSize = rand(0.08, 0.9) - 0.08;

        this.lifetime = (int) rand(32, 0.85);
        this.hasPhysics = true;

        this.setSize(0.01F, 0.01F);
    }


    @Override
    public float getQuadSize(float age) {
        float t = (float) this.age + age;
        double a = 0.15;
        float inc = (float) (this.additionalSize * (1 + 1 / (-t * a - 1)));
        return this.quadSize + inc;
    }

    @Override
    public void tick() {
        if (this.age > 6) this.hasPhysics = true;
        super.tick();
        this.setColorForAge();
    }

    @Override
    public void updateSprite() {
        int i = this.lifetime - this.age;
        int s = 2;
        if (i < 3 * s) {
            int length = 4;
            int j = Math.max(i, 0) / s;
            this.setSprite(this.sprites.get((int) (30 * (3f - j) / (length - 1f)), 30));
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprite;

        public Factory(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ,
                                       double pXSpeed, double pYSpeed, double pZSpeed) {
            Random r = pLevel.random;
            //TODO: add randomness here
            return new SudsParticle(pLevel, pX, pY, pZ,
                    pXSpeed + ((0.5-r.nextFloat()) * 0.04),
                    pYSpeed + ((0.5-r.nextFloat()) * 0.04),
                    pZSpeed + ((0.5-r.nextFloat()) * 0.04), this.sprite);
        }
    }
}