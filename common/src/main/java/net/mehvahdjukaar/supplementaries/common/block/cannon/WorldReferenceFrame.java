package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.mehvahdjukaar.supplementaries.common.block.tiles.CannonBlockTile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class WorldReferenceFrame implements ReferenceFrame {

    private final BlockEntity be;

    public WorldReferenceFrame(BlockEntity be) {
        this.be = be;
    }

    @Override
    public Vec3 position(float partialTicks) {
        return Vec3.atCenterOf(be.getBlockPos());
    }

    @Override
    public Vec3 facing(float partialTicks) {
        return Vec3.ZERO;
    }

    @Override
    public Vec3 velocity() {
        return Vec3.ZERO;
    }

    @Override
    public void applyRecoil(Vec3 recoil) {
    }

    @Override
    public void updateClients() {

    }

    @Override
    public TileOrEntityTarget makeNetworkTarget() {
        return TileOrEntityTarget.of(be);
    }

    @Override
    public int oldGetYawOffset(float partialTicks) {
        return 0;
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
}
