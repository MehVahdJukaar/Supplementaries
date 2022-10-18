package net.mehvahdjukaar.supplementaries.common.utils.fabric;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.supplementaries.client.ClientAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class CommonUtilImpl {

    public static Player getEntityStand(Entity copyPosFrom, Entity copyRotFrom) {
        if(copyRotFrom instanceof Player p)return p;
        if(copyPosFrom instanceof Player p)return p;
        else return null; //TODO: add
    }

    public static Player getFakePlayer(Level serverLevel) {
        return null;
    }
}
