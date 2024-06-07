package net.mehvahdjukaar.supplementaries.common.block.cannon;

import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PopperBehavior implements ICannonBehavior {

    @Override
    public boolean hasBallisticProjectile() {
        return false;
    }

    @Override
    public float getDrag() {
        return 0;
    }

    @Override
    public float getGravity() {
        return 0;
    }

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, BlockPos pos, Vec3 facing, int power, float drag, int inaccuracy, @Nullable Player owner) {
        Vec3 p = new Vec3(pos.getX() + 0.5 - facing.x,
                pos.getY() + 0.5 - facing.y, pos.getZ() + 0.5 - facing.z);

        ClientBoundParticlePacket packet = new ClientBoundParticlePacket(p, ClientBoundParticlePacket.Type.CONFETTI,
                power, facing);
        ModNetwork.CHANNEL.sendToAllClientPlayersInDefaultRange(level, pos, packet);
        return true;
    }

}
