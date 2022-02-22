package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ParticleUtil {
    public static void spawnParticlesOnBlockFaces(Level level, BlockPos pos, ParticleOptions particleOptions,
                                                  UniformInt uniformInt, float minSpeed, float maxSpeed, boolean perpendicular) {
        for (Direction direction : Direction.values()) {
            int i = uniformInt.sample(level.random);

            for (int j = 0; j < i; ++j) {
                spawnParticleOnFace(level, pos, direction, particleOptions, minSpeed, maxSpeed, perpendicular);
            }
        }
    }

    public static void spawnParticleOnFace(Level level, BlockPos pos, Direction direction, ParticleOptions particleOptions,
                                           float minSpeed, float maxSpeed, boolean perpendicular) {
        Vec3 vec3 = Vec3.atCenterOf(pos);
        int i = direction.getStepX();
        int j = direction.getStepY();
        int k = direction.getStepZ();
        double d0 = vec3.x + (i == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : (double) i * 0.6D);
        double d1 = vec3.y + (j == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : (double) j * 0.6D);
        double d2 = vec3.z + (k == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : (double) k * 0.6D);
        double dx;
        double dy;
        double dz;
        if (perpendicular) {
            dx = i * Mth.randomBetween(level.random, minSpeed, maxSpeed);
            dy = j * Mth.randomBetween(level.random, minSpeed, maxSpeed);
            dz = k * Mth.randomBetween(level.random, minSpeed, maxSpeed);
        } else {
            dx = (i == 0) ? maxSpeed * level.random.nextDouble() : 0.0D;
            dy = (j == 0) ? maxSpeed * level.random.nextDouble() : 0.0D;
            dz = (k == 0) ? maxSpeed * level.random.nextDouble() : 0.0D;
        }
        level.addParticle(particleOptions, d0, d1, d2, dx, dy, dz);
    }
}
