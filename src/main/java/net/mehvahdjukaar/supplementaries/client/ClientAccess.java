package net.mehvahdjukaar.supplementaries.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ClientAccess {
    public static Player getFakeClientPlayer(Level level, GameProfile gameProfile){
        return FakeLocalPlayer.get((ClientLevel)level, gameProfile);
    }
}
