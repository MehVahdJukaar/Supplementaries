package net.mehvahdjukaar.supplementaries.client;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.sounds.SoundEvent;

import java.util.Map;

public class FakeLocalPlayer extends AbstractClientPlayer {
    public FakeLocalPlayer(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile,null);
        this.noPhysics = true;
    }

    @Override
    public void playSound(SoundEvent pSound, float pVolume, float pPitch) {
    }

    @Override
    public void tick() {
    }


    @Override
    public void setXRot(float pXRot) {
        super.setXRot(pXRot);
        this.xRotO = pXRot;
    }

    @Override
    public void setYRot(float pYRot) {
        super.setYRot(pYRot);
        this.yRotO = pYRot;
    }

    // Map of all active fake player usernames to their entities
    private static final Map<GameProfile, FakeLocalPlayer> FAKE_PLAYERS = Maps.newHashMap();

    /**
     * Get a fake player with a given username,
     * Mods should either hold weak references to the return value, or listen for a
     * WorldEvent.Unload and kill all references to prevent worlds staying in memory.
     */
    //this better be a client level
    public static FakeLocalPlayer get(ClientLevel level, GameProfile username) {
        if (!FAKE_PLAYERS.containsKey(username)) {
            FakeLocalPlayer fakePlayer = new FakeLocalPlayer(level, username);
            FAKE_PLAYERS.put(username, fakePlayer);
        }

        return FAKE_PLAYERS.get(username);
    }
}
