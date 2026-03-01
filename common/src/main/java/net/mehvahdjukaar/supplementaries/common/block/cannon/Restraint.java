package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record Restraint(float minYaw, float maxYaw,
                        float minPitch, float maxPitch) {

    public Restraint(float minYaw, float maxYaw,
                     float minPitch, float maxPitch) {

        this.minYaw = Mth.wrapDegrees(minYaw);
        this.maxYaw = Mth.wrapDegrees(maxYaw);

        this.minPitch = minPitch;
        this.maxPitch = maxPitch;
    }

    public static final Restraint UNBOUND = new Restraint(-180f, 180f, -90f, 90f);

    private boolean isYawWithin(float yaw) {
        if (minYaw <= maxYaw) {
            return yaw >= minYaw && yaw <= maxYaw;
        } else {
            // wrapped interval (e.g. 300° → 20°)
            return yaw >= minYaw || yaw <= maxYaw;
        }
    }

    public float clampYaw(float yaw) {
        yaw = Mth.wrapDegrees(yaw);

        if (isYawWithin(yaw)) {
            return yaw;
        }

        float distToMin = Mth.degreesDifferenceAbs(yaw, minYaw);
        float distToMax = Mth.degreesDifferenceAbs(yaw, maxYaw);

        return distToMin < distToMax ? minYaw : maxYaw;
    }

    public float clampPitch(float pitch) {
        return Mth.clamp(pitch, minPitch, maxPitch);
    }

    public Quaternionf clampRotation(Quaternionf rotation) {

        // Extract forward direction
        Vector3f forward = new Vector3f(0, 0, 1);
        rotation.transform(forward);

        // Convert direction to yaw/pitch
        float yaw = (float) Math.toDegrees(
                Math.atan2(forward.x, forward.z)
        );

        float pitch = (float) Math.toDegrees(
                Math.asin(-forward.y)
        );

        yaw = Mth.wrapDegrees(yaw);

        // Clamp
        yaw = clampYaw(yaw);
        pitch = clampPitch(pitch);

        // Rebuild quaternion (no roll)
        return new Quaternionf()
                .rotationYXZ(
                        (float) Math.toRadians(yaw),
                        (float) Math.toRadians(pitch),
                        0f
                );
    }

    public Restraint rotated(Direction direction) {
        return switch (direction) {
            case NORTH -> this;
            case SOUTH -> new Restraint(minYaw + 180f, maxYaw + 180f, minPitch, maxPitch);
            case EAST  -> new Restraint(minYaw + 90f,  maxYaw + 90f,  minPitch, maxPitch);
            case WEST  -> new Restraint(minYaw - 90f,  maxYaw - 90f,  minPitch, maxPitch);
            case UP -> new Restraint(minPitch, maxPitch, -maxYaw, -minYaw);
            case DOWN -> new Restraint(minPitch, maxPitch, minYaw, maxYaw);
        };
    }
}