package net.mehvahdjukaar.supplementaries.common.block.cannon;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.mehvahdjukaar.supplementaries.Supplementaries;
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

    private OrientationRig(Quaternionf rot, Quaternionf prevRot) {
        this.rotation = rot;
        this.prevRotation = prevRot;
        this.validateOrientation();
    }

    public OrientationRig() {
        this(new Quaternionf(), new Quaternionf());
    }

    public void tick() {
        this.prevRotation.set(this.rotation);
    }

    public Quaternionf getRotation(float partialTicks) {
        if(true)return new Quaternionf(rotation);
        return new Quaternionf(prevRotation).slerp(rotation, partialTicks);
    }

    public void pointToward(Vec3 target) {
        Vector3f t = target.toVector3f();
        t.normalize();
        Quaternionf targetRotation = new Quaternionf().lookAlong(
                new Vector3f(t).negate(),
                new Vector3f(0, 1, 0)
        );
        this.orient(targetRotation);
    }

    public void orient(Quaternionf quaternionf) {
        this.rotation.set(quaternionf);
        this.validateOrientation();
    }

    private void validateOrientation() {
        if (!this.rotation.isFinite()){
            Supplementaries.error("OrientationRig rotation is not finite");
            this.rotation.set(new Quaternionf());
        }
        if (!this.prevRotation.isFinite()){
            Supplementaries.error("OrientationRig prevRotation is not finite");
            this.prevRotation.set(new Quaternionf());
        }
    }
}