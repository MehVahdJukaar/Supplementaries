package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.minecraft.core.Direction;
import org.joml.Quaternionf;

public final class YawPitchRestraint {

    private final float minYawDeg;
    private final float maxYawDeg;
    private final float minPitchDeg;
    private final float maxPitchDeg;

    public static final YawPitchRestraint UNBOUND = new YawPitchRestraint(-360f, 360f, -360f, 360f);

    public YawPitchRestraint(float minYawDeg, float maxYawDeg, float minPitchDeg, float maxPitchDeg) {

        this.minYawDeg = minYawDeg;
        this.maxYawDeg = maxYawDeg;
        this.minPitchDeg = minPitchDeg;
        this.maxPitchDeg = maxPitchDeg;
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

        EntityAngles angles = EntityAngles.fromQuaternion(rotation);
        angles = angles.clamped(minPitchDeg, maxPitchDeg, minYawDeg, maxYawDeg);

        return angles.toQuaternion();
    }

    public YawPitchRestraint rotated(Direction direction) {
        return switch (direction) {
            case NORTH -> this;
            case SOUTH -> new YawPitchRestraint(minYawDeg + 180f, maxYawDeg + 180f, minPitchDeg, maxPitchDeg);
            case EAST -> new YawPitchRestraint(minYawDeg + 90f, maxYawDeg + 90f, minPitchDeg, maxPitchDeg);
            case WEST -> new YawPitchRestraint(minYawDeg - 90f, maxYawDeg - 90f, minPitchDeg, maxPitchDeg);
            case UP -> new YawPitchRestraint(minPitchDeg, maxPitchDeg, -maxYawDeg, -minYawDeg);
            case DOWN -> new YawPitchRestraint(minPitchDeg, maxPitchDeg, minYawDeg, maxYawDeg);
        };
    }
}