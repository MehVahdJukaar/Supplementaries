package net.mehvahdjukaar.supplementaries.common.utils;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

import java.util.Map;

public class MovableFakePlayer extends FakePlayer {
    public MovableFakePlayer(ServerLevel level, GameProfile name) {
        super(level, name);
    }

    @Override
    public Vec3 position() {
        return new Vec3(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public BlockPos blockPosition() {
        return new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ());
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
    private static final Map<GameProfile, MovableFakePlayer> FAKE_PLAYERS = Maps.newHashMap();

    /**
     * Get a fake player with a given username,
     * Mods should either hold weak references to the return value, or listen for a
     * WorldEvent.Unload and kill all references to prevent worlds staying in memory.
     */
    public static MovableFakePlayer get(ServerLevel level, GameProfile username) {
        if (!FAKE_PLAYERS.containsKey(username)) {
            MovableFakePlayer fakePlayer = new MovableFakePlayer(level, username);
            FAKE_PLAYERS.put(username, fakePlayer);
        }

        return FAKE_PLAYERS.get(username);
    }

    public static void unloadLevel(ServerLevel level) {
        FAKE_PLAYERS.entrySet().removeIf(entry -> entry.getValue().level == level);
    }
}
