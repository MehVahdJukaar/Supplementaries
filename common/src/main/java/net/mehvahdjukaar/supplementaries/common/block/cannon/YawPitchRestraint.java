package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.moonlight.api.util.math.EntityAngles;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;

public record YawPitchRestraint(float minYawDeg, float maxYawDeg, float minPitchDeg, float maxPitchDeg) {

    public static final YawPitchRestraint UNBOUND = new YawPitchRestraint(-360f, 360f, -360f, 360f);

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