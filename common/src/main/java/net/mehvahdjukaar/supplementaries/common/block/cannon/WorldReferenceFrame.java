package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.moonlight.api.misc.TileOrEntityTarget;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

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
    public Quaternionf getRotation(float partialTicks) {
        return new Quaternionf();
    }

    @Override
    public Vec3 velocity() {
        return Vec3.ZERO;
    }

    @Override
    public void applyRecoil(Vec3 recoil) {
    }

    @Override
    public TileOrEntityTarget makeNetworkTarget() {
        return TileOrEntityTarget.of(be);
    }

    @Override
    public boolean isStillValid(Player player) {
        return Container.stillValidBlockEntity(be, player);
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
