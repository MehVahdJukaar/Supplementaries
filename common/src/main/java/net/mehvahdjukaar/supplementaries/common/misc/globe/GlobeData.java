package net.mehvahdjukaar.supplementaries.common.misc.globe;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.client.GlobeManager;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncGlobeDataPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;


public class GlobeData extends SavedData {

    public static final StreamCodec<RegistryFriendlyByteBuf, GlobeData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GlobeData decode(RegistryFriendlyByteBuf buf) {
            int len = buf.readVarInt();
            byte[][] pixels = new byte[len][];
            for (int i = 0; i < len; i++) {
                pixels[i] = buf.readByteArray();
            }
            long seed = buf.readLong();
            return new GlobeData(pixels, seed);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, GlobeData data) {
            buf.writeVarInt(data.globePixels.length);
            for (byte[] pixels : data.globePixels) {
                buf.writeByteArray(pixels);
            }
            buf.writeLong(data.seed);
        }
    };

    private static final int TEXTURE_H = 16;
    private static final int TEXTURE_W = 32;
    public static final String DATA_NAME = "globe_data";

    private final byte[][] globePixels;
    private final long seed;

    public GlobeData(byte[][] pixels, long seed) {
        this.globePixels = pixels;
        this.seed = seed;
    }

    //generate new from seed
    private static GlobeData generate(long seed) {
        return new GlobeData(GlobeTextureGenerator.generate(seed), seed);
    }

    //from tag
    private static GlobeData load(CompoundTag tag, HolderLookup.Provider provider) {
        byte[][] globePixels = new byte[TEXTURE_W][TEXTURE_H];
        for (int i = 0; i < TEXTURE_W; i++) {
            globePixels[i] = tag.getByteArray("colors_" + i);
        }
        long seed = tag.getLong("seed");
        return new GlobeData(globePixels, seed);
    }

    public byte[][] getPixels() {
        return globePixels;
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
                    new Factory<>(() -> GlobeData.generate(server.getSeed()),
                            GlobeData::load, null), DATA_NAME);
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



