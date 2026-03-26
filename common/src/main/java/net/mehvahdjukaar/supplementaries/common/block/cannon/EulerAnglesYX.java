package net.mehvahdjukaar.supplementaries.common.block.cannon;

import com.mojang.serialization.Codec;
import net.frozenblock.lib.shadow.org.jetbrains.annotations.NotNull;
import net.minecraft.util.ExtraCodecs;
import org.joml.Quaternionf;
import org.joml.Vector3f;

// Degrees
// Degrees
public record EulerAnglesYX(float pitch, float yaw) {

    /** Create angles from radians */
    public static EulerAnglesYX fromRadians(float pitchRad, float yawRad) {
        return new EulerAnglesYX(
                (float) Math.toDegrees(pitchRad),
                (float) Math.toDegrees(yawRad)
        );
    }

    /** Extract yaw/pitch from quaternion (roll ignored) */
    public static EulerAnglesYX fromQuaternion(Quaternionf q) {
        // Forward vector along canonical +Z
        Vector3f forward = new Vector3f(0, 0, 1);
        q.transform(forward);
        forward.normalize();

        // Standard JOML right-handed convention: +Y up
        // Yaw = rotation around Y, counterclockwise looking from above
        float yaw   = (float) Math.toDegrees(Math.atan2(forward.x, forward.z));
        float pitch = (float) Math.toDegrees(Math.atan2(forward.y, Math.sqrt(forward.x*forward.x + forward.z*forward.z)));

        return new EulerAnglesYX(pitch, yaw);
    }

    /** Convert to quaternion using standard JOML: Yaw (Y) then Pitch (X) */
    public Quaternionf toQuaternion() {
        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);
        return new Quaternionf().rotationY(yawRad).rotationX(pitchRad);
    }

    public float yawRad() {
        return (float) Math.toRadians(yaw);
    }

    public float pitchRad() {
        return (float) Math.toRadians(pitch);
    }

    /** Apply pitch/yaw clamping */
    public EulerAnglesYX clamp(float minPitch, float maxPitch) {
        return new EulerAnglesYX(Math.max(minPitch, Math.min(maxPitch, pitch)), yaw);
    }

    @Override
    public String toString() {
        return "[pitch=" + pitch + ", yaw=" + yaw + "]";
    }
}