package net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart;

import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.mehvahdjukaar.supplementaries.common.network.ModNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Contract;

public interface ILevelEventRedirect {

    void supp$setRedirected(boolean redirected, Vec3 entityId);

    static boolean supp$tryRedirect(ServerLevel serverLevel, Player pPlayer, Vec3 vec3, int pType, BlockPos pPos, int pData) {
        if (pType == 2000) {
            ModNetwork.CHANNEL.sendToAllClientPlayersInRange( serverLevel, pPos, 64,
                    new ClientBoundParticlePacket(vec3, ClientBoundParticlePacket.Type.DISPENSER_MINECART));
            return true;
        }
        return false;
    }
}
