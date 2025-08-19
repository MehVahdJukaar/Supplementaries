package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class BombChargeParticle extends DirectionOrientedBillboardParticle {

    private final Entity entity;

    protected BombChargeParticle(ClientLevel level, double xOff, double yOff, double zOff, Entity trackingEntity, SpriteSet sprites) {
        super(level, trackingEntity.xo + xOff, trackingEntity.yo + yOff, trackingEntity.zo + zOff);
        this.pickSprite(sprites);
        this.entity = trackingEntity;

        this.lifetime = 200;

        Vec3 sp = entity.getDeltaMovement();
        this.xd = sp.x;
        this.yd = sp.y;
        this.zd = sp.z;
    }

    static Vec3 position(double age, double lifetime,
                         Vec3 center, double R0, double angularSpeed) {
        double progress = age / lifetime;        // goes 0 → 1
        double r = R0 * (1 - progress);          // radius shrinks to 0
        double theta = angularSpeed * age;       // orbit angle

        // spiral coordinates relative to center
        double x = center.x + r * Math.cos(theta);
        double y = center.y + r * Math.sin(theta);
        double z = center.z; // stays at center z (can change if needed)

        return new Vec3(x, y, z);
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

        var p = position(this.age, this.lifetime, entity.position(),
                2, 0.2);

        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
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
                return new BombChargeParticle(worldIn, x, y, z, entity, sprite);
            return null;
        }
    }
}
