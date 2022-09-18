package net.mehvahdjukaar.supplementaries.common.utils.fabric;

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
        if(copyPosFrom instanceof Player p)return p;
        else return null; //TODO: add
    }

    public static Player getFakePlayer(ServerLevel serverLevel) {
        throw new UnsupportedOperationException("Fake player has not been implemented on fabric. This is a bug");
    }
}
