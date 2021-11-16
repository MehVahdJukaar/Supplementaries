package net.mehvahdjukaar.supplementaries.client.particles;

import net.mehvahdjukaar.supplementaries.setup.ModRegistry;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.MetaParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class RotationTrailEmitter extends MetaParticle {

    private final double radius;
    private final int ccw;
    private final Direction axis;

    private int timeSinceStart;

    private RotationTrailEmitter(ClientWorld world, double x, double y, double z, Direction axis, double radius, double velocity) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.radius = radius;
        this.axis = axis;
        this.ccw = velocity > 0 ? -1 : 1;
    }

    @Override
    public void tick() {
        int maximumTime = 6;
        for (int i = 0; i < 4; i++) {

            this.level.addParticle(ModRegistry.ROTATION_TRAIL.get(),
                    this.x, this.y, this.z,
                    this.axis.get3DDataValue() * ccw,
                    this.radius, (i * 90 + 45) + ccw*timeSinceStart*RotationTrailParticle.SPEED);
        }

        ++this.timeSinceStart;
        if (this.timeSinceStart == maximumTime) {
            this.remove();
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class Factory implements IParticleFactory<BasicParticleType> {
        @Override
        public Particle createParticle(BasicParticleType type, ClientWorld world,
                                       double centerX, double centerY, double centerZ,
                                       double direction, double radius, double angularVelocity) {


            return new RotationTrailEmitter(world, centerX, centerY, centerZ,
                    Direction.from3DDataValue((int) direction), radius, angularVelocity);

        }

    }

}
