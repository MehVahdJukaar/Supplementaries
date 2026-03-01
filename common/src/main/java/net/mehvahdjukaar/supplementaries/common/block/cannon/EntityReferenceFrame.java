package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSendKnockbackPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class EntityReferenceFrame implements ReferenceFrame {

    private final Entity entity;

    public EntityReferenceFrame(Entity entity) {
        this.entity = entity;
    }

    @Override
    public Vec3 position(float partialTicks) {
        return entity.getPosition(partialTicks);
    }

    @Override
    public Vec3 facing(float partialTicks) {
        return entity.getViewVector(partialTicks);
    }

    @Override
    public Vec3 velocity() {
        return entity.getDeltaMovement();
    }

    @Override
    public void applyRecoil(Vec3 recoil) {
        if (entity.hasControllingPassenger()) {
            NetworkHelper.sendToAllClientPlayersTrackingEntity(entity,
                    new ClientBoundSendKnockbackPacket(entity.getId(), recoil));
        } else entity.addDeltaMovement(recoil);
    }

    @Override
    public void updateClients() {

    }

    @Override
    public TileOrEntityTarget makeNetworkTarget() {
        return TileOrEntityTarget.of(entity);
    }

    @Override
    public int oldGetYawOffset(float partialTicks) {
        return 0;
    }

    @Override
    public boolean shouldRotatePlayerFaceWhenManeuvering() {
        return false;
    }

    @Override
    public boolean impedePlayerMovementWhenManeuvering() {
        return false;
    }

    @Override
    public boolean canManeuverFromGUI(Player player) {
        return false;
    }
}
