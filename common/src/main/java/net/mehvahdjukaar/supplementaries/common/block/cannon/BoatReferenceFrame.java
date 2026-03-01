package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.supplementaries.common.entities.CannonBoatEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class BoatReferenceFrame extends EntityReferenceFrame{

    private final CannonBoatEntity boat;

    public BoatReferenceFrame(CannonBoatEntity boat) {
        super(boat);
        this.boat = boat;
    }


    @Override
    public Vec3 position(float partialTicks) {
        float yaw = boat.getViewYRot(partialTicks);
        Vec3 vv = boat.getCannonOffset();
        vv = vv.yRot(Mth.DEG_TO_RAD * yaw);
        return boat.getPosition(partialTicks).add(vv);

    }

    @Override
    public int oldGetYawOffset(float partialTicks) {
            return 180 - boat.getViewYRot(partialTicks);
    }

    @Override
    public boolean shouldRotatePlayerFaceWhenManeuvering() {
        return true;
    }

    @Override
    public boolean impedePlayerMovementWhenManeuvering() {
        return false;
    }


    @Override
    public boolean canManeuverFromGUI(Player player) {
        return boat.getControllingPassenger() == player;
    }
}
