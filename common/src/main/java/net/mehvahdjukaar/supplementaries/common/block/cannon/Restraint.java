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

        Quaternionf rotation = new Quaternionf(input).normalize();

        // --- Extract forward ---
        Vector3f forward = new Vector3f(0, 0, 1);
        rotation.transform(forward);

        // --- Clamp pitch ---
        float pitch = (float) Math.asin(-forward.y);
        pitch = (float) Mth.clamp(pitch, Math.toRadians(minPitchDeg), Math.toRadians(maxPitchDeg));
        // --- Clamp yaw ---
        float yaw = (float) Math.atan2(forward.x, forward.z);
        yaw = clampAngleWrapped(yaw, (float) Math.toRadians(minYawDeg), (float) Math.toRadians(maxYawDeg));

        // --- Reconstruct clamped forward vector ---
        float cosPitch = (float) Math.cos(pitch);

        Vector3f clampedForward = new Vector3f(
                (float) (Math.sin(yaw) * cosPitch),
                (float) (-Math.sin(pitch)),
                (float) (Math.cos(yaw) * cosPitch)
        ).normalize();

        // --- Preserve roll ---
        // Compute original roll around forward axis
        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f right = new Vector3f();
        rotation.transform(new Vector3f(1, 0, 0), right);

        // Build base look rotation without roll
        Quaternionf look = new Quaternionf()
                .lookAlong(clampedForward, up);

        // Extract roll from original rotation
        Vector3f baseRight = new Vector3f(1, 0, 0);
        look.transform(baseRight);

        float rollAngle = baseRight.angle(right);
        Vector3f rollAxis = new Vector3f(clampedForward);

        if (rollAxis.dot(new Vector3f(baseRight).cross(right)) < 0f)
            rollAngle = -rollAngle;

        Quaternionf roll = new Quaternionf()
                .fromAxisAngleRad(rollAxis, rollAngle);

        return roll.mul(look).normalize();
    }

    private static float clampAngleWrapped(float angle,
                                           float min,
                                           float max) {

        angle = wrap(angle);
        min = wrap(min);
        max = wrap(max);

        if (min <= max) {
            if (angle < min) return min;
            if (angle > max) return max;
            return angle;
        } else {
            // wrapped interval
            if (angle > max && angle < min) {
                float distMin = angleDistance(angle, min);
                float distMax = angleDistance(angle, max);
                return distMin < distMax ? min : max;
            }
            return angle;
        }
    }

    private static float wrap(float a) {
        while (a <= -Math.PI) a += (float) (Math.PI * 2f);
        while (a > Math.PI) a -= (float) (Math.PI * 2f);
        return a;
    }

    private static float angleDistance(float a, float b) {
        return Math.abs(wrap(a - b));
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