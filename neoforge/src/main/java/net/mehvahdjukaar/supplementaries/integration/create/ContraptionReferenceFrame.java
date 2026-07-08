package net.mehvahdjukaar.supplementaries.integration.create;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.block.cannon.ReferenceFrame;
import net.mehvahdjukaar.supplementaries.common.block.fire_behaviors.BallisticData;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.mehvahdjukaar.supplementaries.integration.CreateCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Optional;

public record ContraptionReferenceFrame(Entity contraption, BlockPos localPos) implements ReferenceFrame {

    @Override
    public Vec3 position(float partialTicks) {
        return CreateCompat.contraptionPosToGlobalPos(contraption, Vec3.atCenterOf(localPos), partialTicks);
    }

    @Override
    public Quaternionf getRotation(float partialTicks) {
        return CreateCompat.getContraptionRotation(contraption, partialTicks);
    }

    @Override
    public Vec3 velocity() {
        return CreateCompat.getContactPointMotion(contraption, position(1));
    }

    @Override
    public void applyRecoil(Vec3 recoil) {
        contraption.addDeltaMovement(recoil);
    }

    @Override
    public TileOrEntityTarget makeNetworkTarget() {
        return TileOrEntityTarget.of(contraption);
    }

    @Override
    public boolean isStillValid(Player player) {
        return !contraption.isRemoved();
    }

    @Override
    public boolean shouldRotatePlayerFaceWhenManeuvering() {
        return true;
    }

    @Override
    public boolean impedePlayerMovementWhenManeuvering() {
        return true;
    }

    @Override
    public boolean canManeuverFromGUI(Player player) {
        return true;
    }

    @Override
    public void syncToServer(CannonBlockTile tile, Quaternionf localRot, byte firePower, boolean ignite,
                             boolean removeOwner, Player player) {
        NetworkHelper.sendToServer(new SyncContraptionCannonPacket(
                contraption.getUUID(), localPos, localRot, firePower, ignite, removeOwner, Optional.empty(),
                player.getUUID()));
    }

    @Override
    public void syncToClients(CannonBlockTile tile, Quaternionf localRot, byte firePower, boolean ignite,
                              Optional<BallisticData> ballisticData) {
        // contraptions have no server-side block entity; relay happens inside SyncContraptionCannonPacket
    }
}
