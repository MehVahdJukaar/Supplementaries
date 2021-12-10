package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.Random;

public class SudsParticle extends TextureSheetParticle {
    private static final Random RANDOM = new Random();
    private final SpriteSet sprites;

    private final float colorRange;
    private final float startingColorInd;
    private final double additionalSize;

    SudsParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ, 0, 0, 0);
        this.xd = pXSpeed;
        this.yd = pYSpeed;
        this.zd = pZSpeed;
        this.friction = 0.96F;
        this.gravity = -0.05F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = pSprites;


        this.additionalSize = rand(0.08, 0.9) - 0.08;

        this.lifetime = (int) rand(32, 0.85);
        this.hasPhysics = true;

        this.colorRange = 0.325f + RANDOM.nextFloat() * 0.5f;
        this.startingColorInd = RANDOM.nextFloat();
        this.setSpriteFromAge(this.sprites);
        this.setColorForAge();
        this.setSize(0.01F, 0.01F);
    }

    private static double r(double a) {
        return a * RANDOM.nextDouble();
    }

    private static double rand(double min, double variation) {
        return (min / (RANDOM.nextFloat() * variation + (1 - variation)));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float t = (float) this.age + partialTicks;
        double a = 0.15;
        float inc = (float) (this.additionalSize * (1 + 1 / (-t * a - 1)));
        return this.quadSize + inc;
    }

    @Override
    public void tick() {
        if (this.age > 6) this.hasPhysics = true;

        int i = this.lifetime - this.age;
        int s = 2;
        if (i < 3 * s) {
            int length = 4;
            int j = Math.max(i, 0) / s;
            this.setSprite(this.sprites.get((int) (30 * (3f - j) / (length - 1f)), 30));
        }
        super.tick();

        this.setAlpha(Mth.lerp(0.05F, this.alpha, 1.0F));

        this.setColorForAge();
    }

    private static final float[][] COLORS;

    static {
        int[] c = new int[]{0xd3a4f7, 0xf3c1f0, 0xd3a4f7, 0xa2c0f8, 0xa2f8df, 0xa2c0f8,};
        float[][] temp = new float[c.length][];
        for (int i = 0; i < c.length; i++) {
            int j = c[i];
            temp[i] = new float[]{FastColor.ARGB32.red(j) / 255f,
                    FastColor.ARGB32.green(j) / 255f, FastColor.ARGB32.blue(j) / 255f};
        }
        COLORS = temp;
    }

    public void setColorForAge() {
        float age = this.age / (float) this.lifetime;
        float a = (age * this.colorRange + this.startingColorInd + 1) % 1;

        int n = COLORS.length;
        int ind = (int) Math.floor(n * a);

        float delta = n * a % 1;

        float[] start = COLORS[ind];
        float[] end = COLORS[(ind + 1) % n];


        float red = Mth.lerp(delta, start[0], end[0]);
        float green = Mth.lerp(delta, start[1], end[1]);
        float blue = Mth.lerp(delta, start[2], end[2]);
        this.rCol = red;
        this.bCol = blue;
        this.gCol = green;
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