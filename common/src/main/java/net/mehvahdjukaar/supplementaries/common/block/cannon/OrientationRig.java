package net.mehvahdjukaar.supplementaries.common.block.cannon;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class OrientationRig {

    public static final Codec<OrientationRig> CODEC =
            ExtraCodecs.QUATERNIONF.xmap(q -> new OrientationRig(q, new Quaternionf(q)),
                    rig -> rig.rotation);
    public static final StreamCodec<ByteBuf, OrientationRig> STREAM_CODEC =
            ByteBufCodecs.QUATERNIONF.map(q -> new OrientationRig(q, new Quaternionf(q)),
                    rig -> rig.rotation);


    private final Quaternionf rotation;
    private final Quaternionf prevRotation;

    public OrientationRig(Quaternionf rot, Quaternionf prevRot) {
        this.rotation = rot;
        this.prevRotation = prevRot;
    }

    public OrientationRig() {
        this(new Quaternionf(), new Quaternionf());
    }

    public void tick() {
        this.prevRotation.set(this.rotation);
    }

    public void pointToward(Vec3 target) {
        Vector3f t = target.toVector3f();
        t.normalize();
        Quaternionf targetRotation = new Quaternionf().lookAlong(
                new Vector3f(t).negate(),
                new Vector3f(0, 1, 0)
        );
        this.rotation.set(targetRotation);
    }

    //cache?
    public Vec3 toForwardVector(float partialTicks) {
        Quaternionf q = getRotation(partialTicks);
        Vector3f forward = new Vector3f(0, 0, 1);
        q.transform(forward);
        return new Vec3(forward);
    }

    public EulerAngles toEulerAngles(float partialTicks) {
        Quaternionf q = getRotation(partialTicks);

        Vector3f forward = new Vector3f(0, 0, 1);
        q.transform(forward);

        float yaw = (float) Math.atan2(forward.x, forward.z);
        float pitch = (float) Math.atan2(
                forward.y,
                Math.sqrt(forward.x * forward.x + forward.z * forward.z)
        );

        return EulerAngles.fromRadians(pitch, yaw, 0);
    }


    public Quaternionf getRotation(float partialTicks) {
        return new Quaternionf(prevRotation).slerp(rotation, partialTicks);
    }

    public void set(Quaternionf quaternionf) {
        this.rotation.set(quaternionf);
        this.prevRotation.set(quaternionf);
    }
}