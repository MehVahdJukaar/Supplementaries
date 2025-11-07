package net.mehvahdjukaar.supplementaries.common.block.fire_behaviors;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PopperBehavior implements IFireItemBehavior {

    @Override
    public boolean fire(ItemStack stack, ServerLevel level, Vec3 firePos, Vec3 direction, float power, int inaccuracy, @Nullable Player owner) {
        ClientBoundParticlePacket packet = new ClientBoundParticlePacket(firePos,
                ClientBoundParticlePacket.Kind.CONFETTI, (int) power, direction);
        BlockPos pos = BlockPos.containing(firePos);

        NetworkHelper.sendToAllClientPlayersInDefaultRange(level, pos, packet);

        level.gameEvent(owner, GameEvent.EXPLODE, firePos);
        return true;
    }

}
