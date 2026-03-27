package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.supplementaries.Supplementaries;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class OrientationRig {

    private final Quaternionf rotation = new Quaternionf();
    private final Quaternionf prevRotation = new Quaternionf();
    private final Quaternionf wantedRotation = new Quaternionf();

    public OrientationRig() {
    }

    public void tick() {
        this.prevRotation.set(this.rotation);
        this.rotation.set(this.wantedRotation);
        this.wantedRotation.set(this.rotation);
    }

    public Quaternionf getRotation(float partialTicks) {
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
        this.wantedRotation.set(quaternionf);
        this.validateOrientation();
    }

    private void validateOrientation() {
        if (!this.rotation.isFinite()) {
            Supplementaries.error("OrientationRig rotation is not finite");
            this.rotation.set(new Quaternionf());
        }
        if (!this.prevRotation.isFinite()) {
            Supplementaries.error("OrientationRig prevRotation is not finite");
            this.prevRotation.set(new Quaternionf());
        }
        if (!this.wantedRotation.isFinite()) {
            Supplementaries.error("OrientationRig prevRotation is not finite");
            this.wantedRotation.set(new Quaternionf());
        }
    }
}