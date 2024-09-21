package net.mehvahdjukaar.supplementaries.common.misc.globe;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.common.components.BlackboardData;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncGlobeDataPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;


public class GlobeData extends SavedData {

    private static final int TEXTURE_H = 16;
    private static final int TEXTURE_W = 32;
    public static final String DATA_NAME = "globe_data";

    public final byte[][] globePixels;
    public final long seed;

    //generate new from seed
    public GlobeData(long seed) {
        this.seed = seed;
        this.globePixels = GlobeTextureGenerator.generate(this.seed);
    }

    //from tag
    public GlobeData(CompoundTag tag, HolderLookup.Provider provider) {
        this.globePixels = new byte[TEXTURE_W][TEXTURE_H];
        for (int i = 0; i < TEXTURE_W; i++) {
            this.globePixels[i] = tag.getByteArray("colors_" + i);
        }
        this.seed = tag.getLong("seed");
    }


    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        for (int i = 0; i < globePixels.length; i++) {
            tag.putByteArray("colors_" + i, this.globePixels[i]);
        }
        tag.putLong("seed", this.seed);
        return tag;
    }

    //call after you modify the data value
    public void sendToClient(Level world) {
        this.setDirty();
        if (!world.isClientSide)
            NetworkHelper.sendToAllClientPlayers(new ClientBoundSyncGlobeDataPacket(this));
    }

    //data received from network is stored here
    private static GlobeData CLIENT_SIDE_INSTANCE = null;

    @Nullable
    public static GlobeData get(Level world) {
        if (world instanceof ServerLevel server) {
            return world.getServer().overworld().getDataStorage().computeIfAbsent(
                    new Factory<>(() -> new GlobeData(server.getSeed()),
                            GlobeData::new, null), DATA_NAME);
        } else {
            return CLIENT_SIDE_INSTANCE;
        }
    }


    public static void set(ServerLevel level, GlobeData pData) {
        level.getServer().overworld().getDataStorage().set(DATA_NAME, pData);
    }

    public static void setClientData(GlobeData data) {
        CLIENT_SIDE_INSTANCE = data;
        GlobeManager.refreshTextures();
    }

    public static void sendDataToClient(ServerPlayer player) {
        GlobeData data = GlobeData.get(player.level());
        if (data != null) {
            NetworkHelper.sendToClientPlayer(player, new ClientBoundSyncGlobeDataPacket(data));
        }

    }
}



