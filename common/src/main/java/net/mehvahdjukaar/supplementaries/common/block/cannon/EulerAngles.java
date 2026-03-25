package net.mehvahdjukaar.supplementaries.common.block.cannon;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

//Degrees pls
public record EulerAngles(float pitch, float yaw, float roll) {

    public static final Codec<EulerAngles> CODEC = ExtraCodecs.VECTOR3F
            .xmap(v -> new EulerAngles(v.x, v.y, v.z), EulerAngles::toVector3f);

    public static EulerAngles of(float pitch, float yaw, float roll) {
        return new EulerAngles(pitch, yaw, roll);
    }

    public static EulerAngles ofPitchAndYaw(float pitch, float yaw) {
        return of(pitch, yaw, 0);
    }

    public static EulerAngles fromRadians(float pitchRad, float yawRad, float rollRad) {
        return new EulerAngles((float) Math.toDegrees(pitchRad), (float) Math.toDegrees(yawRad), (float) Math.toDegrees(rollRad));
    }

    public static EulerAngles fromRotation(Quaternionf q) {
        var v = new Vector3f();
        q.getEulerAnglesYXZ(v);
        return EulerAngles.fromRadians(v.x, v.y, v.z);
    }

    public Vec3 toVec3() {
        return new Vec3(pitch, yaw, roll);
    }

    public Vector3f toVector3f() {
        return new Vector3f(pitch, yaw, roll);
    }

    public EulerAngles add(EulerAngles other) {
        return new EulerAngles(pitch + other.pitch, yaw + other.yaw, roll + other.roll);
    }

    public EulerAngles add(float dpitch, float dyaw, float droll) {
        return new EulerAngles(pitch + dpitch, yaw + dyaw, roll + droll);
    }

    public Quaternionf toQuaternion() {
        //TODO: verify order
        return new Quaternionf().rotationYXZ(
                (float) Math.toRadians(Mth.wrapDegrees(yaw)),
                (float) Math.toRadians(pitch),
                (float) Math.toRadians(roll)
        );
    }

    @Override
    public @NotNull String toString() {
        return "[pitch=" + pitch + ", yaw=" + yaw + ", roll=" + roll + "]";
    }
}
