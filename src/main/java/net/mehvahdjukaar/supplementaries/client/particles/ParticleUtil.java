package net.mehvahdjukaar.supplementaries.client.particles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class ParticleUtil {

    //call with packet

    public static void spawnParticleOnBlockShape(Level level, BlockPos pos, ParticleOptions particleOptions,
                                                  UniformInt uniformInt, float maxSpeed) {
        spawnParticleOnBoundingBox(level.getBlockState(pos).getShape(level, pos).bounds(), level, pos,
                particleOptions, uniformInt, maxSpeed);
    }

    public static void spawnParticleOnBoundingBox(AABB bb, Level level, BlockPos pos, ParticleOptions particleOptions,
                                                  UniformInt uniformInt, float maxSpeed) {

        Random random = level.random;
        float offset = 0.1f;

        //north
        int i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            if (x > bb.minX && x < bb.maxX && y > bb.minY && y < bb.maxY) {
                double dx = maxSpeed * level.random.nextDouble();
                double dy = maxSpeed * level.random.nextDouble();
                double dz = 0;
                level.addParticle(particleOptions, pos.getX() + x, pos.getY() + y, pos.getZ() + bb.minZ-offset, dx, dy, dz);
            }
        }
        //south
        i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            if (x > bb.minX && x < bb.maxX && y > bb.minY && y < bb.maxY) {
                double dx = maxSpeed * level.random.nextDouble();
                double dy = maxSpeed * level.random.nextDouble();
                double dz = 0;
                level.addParticle(particleOptions, pos.getX() + x, pos.getY() + y, pos.getZ() + bb.maxZ+offset, dx, dy, dz);
            }
        }
        //west
        i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double z = random.nextDouble();
            double y = random.nextDouble();
            if (z > bb.minZ && z < bb.maxZ && y > bb.minY && y < bb.maxY) {
                double dx = 0;
                double dy = maxSpeed * level.random.nextDouble();
                double dz = maxSpeed * level.random.nextDouble();
                level.addParticle(particleOptions, pos.getX() + bb.minX-offset, pos.getY() + y, pos.getZ() + z, dx, dy, dz);
            }
        }
        //east
        i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double z = random.nextDouble();
            double y = random.nextDouble();
            if (z > bb.minZ && z < bb.maxZ && y > bb.minY && y < bb.maxY) {
                double dx = 0;
                double dy = maxSpeed * level.random.nextDouble();
                double dz = maxSpeed * level.random.nextDouble();
                level.addParticle(particleOptions, pos.getX() + bb.maxX+offset, pos.getY() + y, pos.getZ() + z, dx, dy, dz);
            }
        }
        //down
        i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double x = random.nextDouble();
            double z = random.nextDouble();
            if (x > bb.minX && x < bb.maxX && z > bb.minZ && z < bb.maxZ) {
                double dx = maxSpeed * level.random.nextDouble();
                double dy = 0;
                double dz = maxSpeed * level.random.nextDouble();
                level.addParticle(particleOptions, pos.getX() + x, pos.getY() + bb.minY-offset, pos.getZ() + z, dx, dy, dz);
            }
        }
        //up
        i = uniformInt.sample(random);
        for (int j = 0; j < i; ++j) {
            double x = random.nextDouble();
            double z = random.nextDouble();
            if (x > bb.minX && x < bb.maxX && z > bb.minZ && z < bb.maxZ) {
                double dx = maxSpeed * level.random.nextDouble();
                double dy = 0;
                double dz = maxSpeed * level.random.nextDouble();
                level.addParticle(particleOptions, pos.getX() + x, pos.getY() + bb.maxY+offset, pos.getZ() + z, dx, dy, dz);
            }
        }
    }


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

    public enum EventType {
        BUBBLE_BLOW,
        BUBBLE_CLEAN,
        DISPENSER_MINECART
    }
}
