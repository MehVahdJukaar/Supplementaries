package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;


public class RotationTrailEmitter extends NoRenderParticle {

    private final double radius;
    private final int ccw;
    private final Direction axis;

    private int timeSinceStart;

    private RotationTrailEmitter(ClientLevel world, double x, double y, double z, Direction axis, double radius, double velocity) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.radius = radius;
        this.axis = axis;
        this.ccw = velocity > 0 ? -1 : 1;
    }

    @Override
    public void tick() {
        int maximumTime = 6;
        for (int i = 0; i < 4; i++) {

            this.level.addParticle(ModParticles.ROTATION_TRAIL.get(),
                    this.x, this.y, this.z,
                    this.axis.get3DDataValue() * ccw,
                    this.radius, (i * 90 + 45) + ccw * timeSinceStart * RotationTrailParticle.SPEED);
        }

        ++this.timeSinceStart;
        if (this.timeSinceStart == maximumTime) {
            this.remove();
        }
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        public Factory(SpriteSet sprite) {
        }


        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world,
                                       double centerX, double centerY, double centerZ,
                                       double direction, double radius, double angularVelocity) {

            Direction dir = Direction.from3DDataValue((int) direction);
            return new RotationTrailEmitter(world, centerX, centerY, centerZ,
                    dir, radius, angularVelocity);

        }

    }

}
