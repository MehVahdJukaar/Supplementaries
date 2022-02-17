package net.mehvahdjukaar.supplementaries.common.world.data;

import net.mehvahdjukaar.supplementaries.client.renderers.GlobeTextureManager;
import net.mehvahdjukaar.supplementaries.common.network.ClientBoundSyncGlobeDataPacket;
import net.mehvahdjukaar.supplementaries.common.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;


public class GlobeData extends SavedData {
    private static final int TEXTURE_H = 16;
    private static final int TEXTURE_W = 32;
    public static final String DATA_NAME = "globe_data";

    public final byte[][] globePixels;
    public final long seed;

    //generate new from seed
    public GlobeData(long seed) {
        this.seed = seed;
        this.globePixels = GlobeDataGenerator.generate(this.seed);
    }

    //from tag
    public GlobeData(CompoundTag tag) {
        this.globePixels = new byte[TEXTURE_W][TEXTURE_H];
        for (int i = 0; i < TEXTURE_W; i++) {
            this.globePixels[i] = tag.getByteArray("colors_" + i);
        }
        this.seed = tag.getLong("seed");
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        for (int i = 0; i < globePixels.length; i++) {
            nbt.putByteArray("colors_" + i, this.globePixels[i]);
        }
        nbt.putLong("seed", this.seed);
        return nbt;
    }

    //call after you modify the data value
    public void sendToClient(Level world) {
        this.setDirty();
        if (!world.isClientSide)
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientBoundSyncGlobeDataPacket(this));
    }

    //data received from network is stored here
    private static GlobeData clientSide = null;

    @Nullable
    public static GlobeData get(Level world) {
        if (world instanceof ServerLevel server) {
            return world.getServer().overworld().getDataStorage().computeIfAbsent(GlobeData::new,
                    () -> new GlobeData(server.getSeed()),
                    DATA_NAME);
        } else {
            return clientSide;
        }
    }

    public static void set(ServerLevel level, GlobeData pData) {
        level.getServer().overworld().getDataStorage().set(DATA_NAME, pData);
    }

    public static void setClientData(GlobeData data) {
        clientSide = data;
        GlobeTextureManager.INSTANCE.update();
    }


    public static void sendGlobeData(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide) {
            GlobeData data = GlobeData.get(event.getPlayer().level);
            if (data != null) {
                NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getPlayer()),
                        new ClientBoundSyncGlobeDataPacket(data));
            }
        }
    }
}



