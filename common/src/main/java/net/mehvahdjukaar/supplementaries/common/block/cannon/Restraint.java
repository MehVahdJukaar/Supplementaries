package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class Restraint {

    private final float minYawDeg;
    private final float maxYawDeg;
    private final float minPitchDeg;
    private final float maxPitchDeg;

    public static final Restraint UNBOUND = new Restraint(-360f, 360f, -360f, 360f);

    public Restraint(float minYawDeg, float maxYawDeg, float minPitchDeg, float maxPitchDeg) {

        this.minYawDeg = minYawDeg;
        this.maxYawDeg = maxYawDeg;
        this.minPitchDeg = minPitchDeg;
        this.maxPitchDeg = maxPitchDeg;
        //TODO: use Mth and be in degrees instead of radians
    }

    public float getMinYaw() {
        return minYawDeg;
    }

    public float getMaxYaw() {
        return maxYawDeg;
    }

    public float getMinPitch() {
        return minPitchDeg;
    }

    public float getMaxPitch() {
        return maxPitchDeg;
    }


    public Quaternionf clamp(Quaternionf input) {
        if(true)return input;
        // Normalize the input quaternion
        Quaternionf rotation = new Quaternionf(input).normalize();

        // Convert quaternion to Euler angles in degrees
        EulerAngles angles = EulerAngles.fromRotation(rotation);

        // Wrap and clamp yaw
        float clampedYaw = Mth.clamp(Mth.wrapDegrees(angles.yaw()), minYawDeg, maxYawDeg);
        // Wrap and clamp pitch
        float clampedPitch = Mth.clamp(Mth.wrapDegrees(angles.pitch()), minPitchDeg, maxPitchDeg);

        // Return the quaternion reconstructed from clamped Euler angles
        return new EulerAngles(clampedYaw, clampedPitch, angles.roll()).toQuaternion();
    }
    public Restraint rotated(Direction direction) {
        return switch (direction) {
            case NORTH -> this;
            case SOUTH -> new Restraint(minYawDeg + 180f, maxYawDeg + 180f, minPitchDeg, maxPitchDeg);
            case EAST -> new Restraint(minYawDeg + 90f, maxYawDeg + 90f, minPitchDeg, maxPitchDeg);
            case WEST -> new Restraint(minYawDeg - 90f, maxYawDeg - 90f, minPitchDeg, maxPitchDeg);
            case UP -> new Restraint(minPitchDeg, maxPitchDeg, -maxYawDeg, -minYawDeg);
            case DOWN -> new Restraint(minPitchDeg, maxPitchDeg, minYawDeg, maxYawDeg);
        };
    }
}