package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class WindTrailParticle extends DirectionOrientedBillboardParticle {

    private final float maxAlpha;
    private final Entity entity;
    private final double xOff;
    private final double yOff;
    private final double zOff;

    protected WindTrailParticle(ClientLevel level, double xOff, double yOff, double zOff, Entity trackingEntity, SpriteSet sprites) {
        super(level, trackingEntity.xo + xOff, trackingEntity.yo + yOff, trackingEntity.zo + zOff);
        this.pickSprite(sprites);
        this.entity = trackingEntity;
        this.xOff = xOff;
        this.yOff = yOff;
        this.zOff = zOff;

        //todo: this determines animation speed essentially. make it depend on speed. or make age depend on speed
        this.lifetime = 5 + random.nextInt(10);

        double normalizedTick = (double) entity.tickCount / lifetime;

        // Map normalized value to the desired age transition
        if (normalizedTick < 0.5) {
            // As tickCount increases from 0 to lifetime/2, age increases smoothly from 0 to maximum
            this.age += random.nextInt(Mth.ceil((0.5 - normalizedTick) * lifetime));
        }

        this.quadSize *= 7f;

        this.maxAlpha = 0.3f + random.nextFloat() * 0.65f;

        Vec3 sp = entity.getDeltaMovement();
        this.xd = sp.x;
        this.yd = sp.y;
        this.zd = sp.z;
        this.updateAlpha();
    }

    private void updateAlpha() {
        this.alpha = (float) (this.maxAlpha * Mth.clamp(this.speed() * 1 - 0.15, 0, 1));
        float percentage = (this.age / (float) this.lifetime);
        // Apply fading effect towards the end of the lifetime
        float fadeStart = 0.5f; // Start fading when 80% of the lifetime is reached
        if (percentage > fadeStart) {
            float fadeFactor = (1.0f - percentage) / (1.0f - fadeStart);
            this.alpha *= fadeFactor;
        }
    }

    private double speed() {
        return Math.sqrt(this.xd * this.xd + this.yd * this.yd + this.zd * this.zd);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (entity == null || entity.isRemoved()) {
            remove();
            return;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.x = entity.xo + xOff;
        this.y = entity.yo + yOff;
        this.z = entity.zo + zOff;
        Vec3 sp = entity.getDeltaMovement();
        this.xd = sp.x;
        this.yd = sp.y;
        this.zd = sp.z;

        this.updateAlpha();
    }

    @Override
    protected float getU0() {
        return super.getU0();
    }

    @Override
    protected float getV0() {
        float p = (this.age / (float) this.lifetime);
        return sprite.getV((p * 16) / 24 * 16);
    }

    @Override
    protected float getV1() {
        float p = (this.age / (float) this.lifetime);
        return sprite.getV((8 + p * 16) / 24 * 16);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Factory(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z,
                                       double entityId, double a, double b) {
            Entity entity = worldIn.getEntity((int) entityId);
            if (entity != null)
                return new WindTrailParticle(worldIn, x, y, z, entity, sprite);
            return null;
        }
    }
}
