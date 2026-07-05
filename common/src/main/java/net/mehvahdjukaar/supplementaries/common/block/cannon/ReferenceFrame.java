package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.BallisticData;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.common.network.SyncCannonPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Optional;

public interface ReferenceFrame {
    Vec3 position(float partialTicks);

    Quaternionf getRotation(float partialTicks);

    Vec3 velocity();

    void applyRecoil(Vec3 recoil);

    TileOrEntityTarget makeNetworkTarget();

    boolean isStillValid(Player player);

    boolean shouldRotatePlayerFaceWhenManeuvering();

    boolean impedePlayerMovementWhenManeuvering();

    boolean canManeuverFromGUI(Player player);

    /**
     * Send a locally-made aim change to the server. Frames whose cannon has no addressable server-side block
     * entity (e.g. inside a Create contraption) override this with their own packet.
     */
    default void syncToServer(CannonBlockTile tile, Quaternionf localRot, byte firePower, boolean ignite,
                              boolean removeOwner, Player player) {
        NetworkHelper.sendToServer(new SyncCannonPacket(
                localRot, firePower, ignite, removeOwner, Optional.empty(),
                makeNetworkTarget(), player.getUUID()));
    }

    default void syncToClients(CannonBlockTile tile, Quaternionf localRot, byte firePower, boolean ignite,
                               Optional<BallisticData> ballisticData) {
        if (tile.getLevel() instanceof ServerLevel sl) {
            NetworkHelper.sendToAllClientPlayersInDefaultRange(sl,
                    BlockPos.containing(position(1)),
                    new SyncCannonPacket(localRot, firePower, ignite, false, ballisticData,
                            makeNetworkTarget(), null));
        }
    }
}

