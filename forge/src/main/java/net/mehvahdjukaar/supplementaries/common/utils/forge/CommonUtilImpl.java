package net.mehvahdjukaar.supplementaries.common.utils.forge;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.supplementaries.client.ClientAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class CommonUtilImpl {

    public static final GameProfile DUMMY_PROFILE = new GameProfile(
            UUID.fromString("9bf808b4-d64a-47f0-9220-e3849f80f35b"), "[player_stando]");


    public static Player getEntityStand(Entity copyPosFrom, Entity copyRotFrom) {
        Level level = copyPosFrom.getLevel();
        Player p;
        if (level instanceof ServerLevel serverLevel) {
            p = MovableFakePlayer.get(serverLevel, DUMMY_PROFILE);
        } else {
            p = ClientAccess.getFakeClientPlayer(level, DUMMY_PROFILE);
        }
        p.setPos(copyPosFrom.getX(), copyPosFrom.getY(), copyPosFrom.getZ());
        p.setYHeadRot(copyRotFrom.getYHeadRot());
        p.setXRot(copyRotFrom.getXRot());
        p.setYRot(copyRotFrom.getYRot());
        p.setOldPosAndRot();
        return p;
    }
}
