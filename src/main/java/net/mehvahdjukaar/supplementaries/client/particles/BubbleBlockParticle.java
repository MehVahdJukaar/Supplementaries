package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BubbleBlockParticle extends TextureSheetParticle {
    private static final Random RANDOM = new Random();
    protected final SpriteSet sprites;

    private final float colorRange;
    private final float startingColorInd;

    public BubbleBlockParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet pSprites) {
        super(pLevel, pX, pY, pZ, 0, 0, 0);
        this.xd = pXSpeed;
        this.yd = pYSpeed;
        this.zd = pZSpeed;

        this.sprites = pSprites;

        this.lifetime = (int) rand(32, 0.85);

        this.colorRange = 0.325f + RANDOM.nextFloat() * 0.5f;
        this.startingColorInd = RANDOM.nextFloat();
        this.setSpriteFromAge(this.sprites);
        this.setColorForAge();
    }

    @Override
    public void tick() {
        super.tick();
        this.updateSprite();
        this.setAlpha(Mth.lerp(0.05F, this.alpha, 1.0F));
    }

    public void updateSprite(){
        this.setSpriteFromAge(this.sprites);
    }

    protected static double rand(double min, double variation) {
        return (min / (RANDOM.nextFloat() * variation + (1 - variation)));
    }

    public void setColorForAge() {
        float age = this.age / (float) this.lifetime;
        float a = (age * this.colorRange + this.startingColorInd + 1) % 1;

        float[] color = ColorHelper.getBubbleColor(a);
        this.rCol = color[0];
        this.gCol = color[1];
        this.bCol = color[2];
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float age) {
        return 0.825F;
    }


    public static class Factory implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprite;

        public Factory(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ,
                                       double pXSpeed, double pYSpeed, double pZSpeed) {
            var p = new BubbleBlockParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, sprite);
            p.gravity = 0.0F;
            p.lifetime = 8;
            p.hasPhysics = false;
            return p;
        }
    }
}
