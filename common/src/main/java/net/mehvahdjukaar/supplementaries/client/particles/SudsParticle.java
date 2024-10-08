package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class SudsParticle extends BubbleBlockParticle {

    protected static final int POP_FRAMES = 4;

    private final double additionalSize;

    SudsParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, pSprites);
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
        if (this.age > 6) {
            this.hasPhysics = true;
        }
        super.tick();
        this.setColorForAge();
    }

    @Override
    public void move(double x, double y, double z) {
        super.move(x, y, z);
        if (hasPhysics && this.age < this.lifetime - POP_FRAMES) {
            Vec3 myPos = new Vec3(this.x, this.y, this.z);
            Vec3 wantedPos = new Vec3(this.xo + x, this.yo + y, this.zo + z);
            if (myPos.distanceToSqr(wantedPos) > 0.000001) {
                // collided with any block. pop. It fragile
                this.age = this.lifetime - POP_FRAMES;
                this.xd = 0;
                this.yd = 0;
                this.zd = 0;
            }
        }
    }

    @Override
    public void updateSprite() {
        int i = this.lifetime - this.age;
        int s = 2;
        if (i < 3 * s) {
            int j = Math.max(i, 0) / s;
            int popTime = 30;
            this.setSprite(this.sprites.get((int) (popTime * (3f - j) / (POP_FRAMES - 1f)), popTime));
            if (gravity != 0) {
                level.playLocalSound(x, y, z, ModSounds.BUBBLE_POP.get(), SoundSource.BLOCKS, 0.15f,
                        2f - this.quadSize * 0.2f, false);
                this.gravity = 0;
                this.yd = 0;
            }
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprite;

        public Factory(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ,
                                       double pXSpeed, double pYSpeed, double pZSpeed) {
            RandomSource r = pLevel.random;
            return new SudsParticle(pLevel, pX, pY, pZ,
                    pXSpeed + ((0.5 - r.nextFloat()) * 0.04),
                    pYSpeed + ((0.5 - r.nextFloat()) * 0.04),
                    pZSpeed + ((0.5 - r.nextFloat()) * 0.04), this.sprite);
        }
    }
}