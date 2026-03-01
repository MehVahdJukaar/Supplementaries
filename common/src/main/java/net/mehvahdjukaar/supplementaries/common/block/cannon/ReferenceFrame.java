package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ReferenceFrame {
    Vec3 position(float partialTicks);

    Vec3 facing(float partialTicks);

    Vec3 velocity();

    void applyRecoil(Vec3 recoil);

    void updateClients();

    TileOrEntityTarget makeNetworkTarget();

    int oldGetYawOffset(float partialTicks);


    boolean shouldRotatePlayerFaceWhenManeuvering();

    boolean impedePlayerMovementWhenManeuvering();

    boolean canManeuverFromGUI(Player player);
}

