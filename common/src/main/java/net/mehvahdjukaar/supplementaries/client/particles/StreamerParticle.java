package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

import java.util.List;

public class StreamerParticle extends DirectionOrientedBillboardParticle {

    // yes we sample 36 perlin noises per tick per particle
    protected static final PerlinSimplexNoise NOISE = noise(404);

    private static PerlinSimplexNoise noise(int seed) {
        return new PerlinSimplexNoise(new LegacyRandomSource(seed),
                List.of(-4 - 3, -2, -1, 0, 1, 2));
    }

    private final int particleRandom;

    private StreamerParticle(ClientLevel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        this.particleRandom = random.nextInt();
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;

        this.setSize(0.01F, 0.01F);
        this.quadSize = 0.3f;
        this.gravity = 0.21F;
        this.friction = 0.95f;
        this.lifetime = random.nextInt(400, 700);
    }


    @Override
    public void tick() {
        boolean still = this.x == this.xo && this.z == this.zo && this.y == this.yo && this.age != 0;

        boolean hasLanded = (onGround || still);

        float posChange = 0.003f;
        this.xd += posChange * NOISE.getValue(particleRandom, this.age, false);
        this.zd += posChange * NOISE.getValue(particleRandom, this.age, false);

        if (hasLanded) {
            this.age = Math.max(this.age, this.lifetime - 5);
        }

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
            var p = new StreamerParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            p.pickSprite(sprite);
            return p;
        }
    }

    public static class DyeFactory implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprite;

        public DyeFactory(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(ColorParticleOption opt, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            var p = new StreamerParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
            p.pickSprite(sprite);
            p.setColor(opt.getRed(), opt.getGreen(), opt.getBlue());
            return p;
        }
    }

}