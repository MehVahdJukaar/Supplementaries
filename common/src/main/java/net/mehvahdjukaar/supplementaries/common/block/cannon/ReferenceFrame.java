package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface ReferenceFrame {
    Vec3 position(float partialTicks);

    Vec3 facing(float partialTicks);

    default Quaternionf rotation(float partialTicks) {
        Vec3 facing = facing(partialTicks).normalize();

        Vector3f forward = new Vector3f((float) facing.x, (float) facing.y, (float) facing.z);
        Vector3f up = new Vector3f(0f, 1f, 0f);

        return new Quaternionf().lookAlong(forward.negate(), up);
    }

    Vec3 velocity();

    void applyRecoil(Vec3 recoil);

    void updateClients();

    TileOrEntityTarget makeNetworkTarget();

    int oldGetYawOffset(float partialTicks);


    boolean shouldRotatePlayerFaceWhenManeuvering();

    boolean impedePlayerMovementWhenManeuvering();

    boolean canManeuverFromGUI(Player player);
}

