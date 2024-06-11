package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PopperBehavior implements IFireItemBehavior {

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction, float power, float drag, int inaccuracy, @Nullable Player owner) {
        ClientBoundParticlePacket packet = new ClientBoundParticlePacket(firePos,
                ClientBoundParticlePacket.Type.CONFETTI, (int) power, direction);
        ModNetwork.CHANNEL.sendToAllClientPlayersInDefaultRange(level, BlockPos.containing(firePos), packet);
        return true;
    }

}
