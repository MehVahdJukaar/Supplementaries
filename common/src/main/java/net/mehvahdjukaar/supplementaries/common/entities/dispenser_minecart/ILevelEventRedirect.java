package net.mehvahdjukaar.supplementaries.common.entities.dispenser_minecart;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundParticlePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ILevelEventRedirect {

    void supp$setRedirected(boolean redirected, Vec3 entityId);

    static boolean supp$tryRedirect(ServerLevel serverLevel, Player pPlayer, Vec3 vec3, int pType, BlockPos pPos, int pData) {
        if (pType == 2000) {
            NetworkHelper.sendToAllClientPlayersInRange( serverLevel, pPos, 64,
                    new ClientBoundParticlePacket(vec3, ClientBoundParticlePacket.Kind.DISPENSER_MINECART));
            return true;
        }
        return false;
    }
}
