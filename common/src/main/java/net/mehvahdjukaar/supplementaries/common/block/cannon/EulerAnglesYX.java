package net.mehvahdjukaar.supplementaries.common.block.cannon;

import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

// Degrees.
// These hold pitch and yaw in an entity like fashion. so yaw is inverted
public record EulerAnglesYX(float pitch, float yaw) {

    private static final Codec<Vector2f> VEC2 = Codec.FLOAT.listOf()
            .comapFlatMap((list) -> Util.fixedSize(list, 2).map((listx) -> new Vector2f(listx.get(0), listx.get(1))), (vector3f) -> List.of(vector3f.x(), vector3f.y()));

    private static final Codec<EulerAnglesYX> CODEC = VEC2.xmap(
            vector2f -> new EulerAnglesYX(vector2f.x, vector2f.y),
            eulerAnglesYX -> new Vector2f(eulerAnglesYX.pitch, eulerAnglesYX.yaw));

    /**
     * Create angles from radians
     */
    public static EulerAnglesYX fromRadians(float pitchRad, float yawRad) {
        return new EulerAnglesYX(
                (float) Math.toDegrees(pitchRad),
                (float) Math.toDegrees(yawRad)
        );
    }

    /**
     * Extract yaw/pitch from quaternion (roll ignored)
     */
    public static EulerAnglesYX fromQuaternion(Quaternionf q) {
        // Forward vector along canonical +Z
        Vector3f forward = new Vector3f(0, 0, 1);
        q.transform(forward);
        forward.normalize();

        // Standard JOML right-handed convention: +Y up
        // Yaw = rotation around Y, counterclockwise looking from above
        float yawRad = (float) -Mth.atan2(forward.x, forward.z);
        float pitchRad = (float) Mth.atan2(forward.y, Mth.sqrt(forward.x * forward.x + forward.z * forward.z));

        return fromRadians(pitchRad, yawRad);
    }

    public static EulerAnglesYX of(float pitch, float yaw) {
        return new EulerAnglesYX(pitch, yaw);
    }

    /**
     * Convert to quaternion using standard JOML: Yaw (Y) then Pitch (X)
     */
    public Quaternionf toQuaternion() {
        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);
        return new Quaternionf().rotateY(-yawRad).rotateX(pitchRad);
    }

    public float yawRad() {
        return (float) Math.toRadians(yaw);
    }

    public float pitchRad() {
        return (float) Math.toRadians(pitch);
    }

    /**
     * Apply pitch/yaw clamping
     */
    public EulerAnglesYX clamp(float minPitch, float maxPitch) {
        return new EulerAnglesYX(Math.max(minPitch, Math.min(maxPitch, pitch)), yaw);
    }

    @Override
    public String toString() {
        return "[pitch=" + pitch + ", yaw=" + yaw + "]";
    }
}